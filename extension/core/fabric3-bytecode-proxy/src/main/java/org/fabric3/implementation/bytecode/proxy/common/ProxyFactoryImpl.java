/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.fabric3.implementation.bytecode.proxy.common;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.oasisopen.sca.annotation.Reference;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * Implementation that uses ASM for bytecode generation.
 */
public class ProxyFactoryImpl implements ProxyFactory, ClassLoaderListener {
    private ClassLoaderRegistry classLoaderRegistry;

    private Map<URI, BytecodeClassLoader> classLoaderCache = new HashMap<URI, BytecodeClassLoader>();

    public ProxyFactoryImpl(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public <T> T createProxy(URI classLoaderKey, Class<T> interfaze, Method[] methods, Class<? extends ProxyDispatcher> dispatcher, boolean wrapped)
            throws ProxyException {
        if (wrapped) {
            return createWrappedProxy(classLoaderKey, interfaze, methods, dispatcher);
        } else {
            return createUnWrappedProxy(classLoaderKey, interfaze, methods, dispatcher);
        }
    }

    public void onDeploy(ClassLoader classLoader) {
        // no-op
    }

    public void onUndeploy(ClassLoader classLoader) {
        if (!(classLoader instanceof MultiParentClassLoader)) {
            return;
        }
        // remove cached classloader for the contribution on undeploy
        classLoaderCache.remove(((MultiParentClassLoader) classLoader).getName());
    }

    /**
     * Creates a proxy that wraps parameters in an object array like JDK proxies when invoking a {@link ProxyDispatcher}.
     *
     * @param classLoaderKey the key for the classloader that the proxy interface is to be loaded in
     * @param interfaze      the proxy interface
     * @param methods        the proxy methods
     * @param dispatcher     the dispatcher
     * @return the proxy
     * @throws ProxyException if there is an error creating the proxy
     */
    @SuppressWarnings("unchecked")
    private <T> T createWrappedProxy(URI classLoaderKey, Class<T> interfaze, Method[] methods, Class<? extends ProxyDispatcher> dispatcher)
            throws ProxyException {

        String className = interfaze.getName() + "_Proxy_" + dispatcher.getSimpleName();  // ensure multiple dispatchers can be defined for the same interface

        // check if the proxy class has already been created
        BytecodeClassLoader generationLoader = getClassLoader(classLoaderKey);
        try {
            Class<T> proxyClass = (Class<T>) generationLoader.loadClass(className);
            return proxyClass.newInstance();
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (InstantiationException e) {
            throw new ProxyException(e);
        } catch (IllegalAccessException e) {
            throw new ProxyException(e);
        }

        String interfazeName = Type.getInternalName(interfaze);
        String handlerName = Type.getInternalName(dispatcher);
        String handlerDescriptor = Type.getDescriptor(dispatcher);
        String classNameInternal = Type.getInternalName(interfaze) + "_Proxy_" + dispatcher.getSimpleName();

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, classNameInternal, null, handlerName, new String[]{interfazeName});

        cw.visitSource(interfaze.getName() + "Proxy.java", null);

        // write the ctor
        writeConstructor(handlerName, handlerDescriptor, cw);

        // write the methods
        int methodIndex = 0;
        for (Method method : methods) {
            String methodSignature = Type.getMethodDescriptor(method);
            String[] exceptions = new String[method.getExceptionTypes().length];
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = Type.getInternalName(method.getExceptionTypes()[i]);
            }
            mv = cw.visitMethod(ACC_PUBLIC, method.getName(), methodSignature, null, exceptions);
            mv.visitCode();

            List<Label> exceptionLabels = new ArrayList<Label>();
            Label label2 = new Label();
            Label label3 = new Label();

            for (String exception : exceptions) {
                Label endLabel = new Label();
                exceptionLabels.add(endLabel);
                mv.visitTryCatchBlock(label2, label3, endLabel, exception);

            }

            mv.visitLabel(label2);
            mv.visitVarInsn(ALOAD, 0);

            // set the method index used to dispatch on
            if (methodIndex >= 0 && methodIndex <= 5) {
                // use an integer constant if within range
                mv.visitInsn(Opcodes.ICONST_0 + methodIndex);
            } else {
                mv.visitIntInsn(Opcodes.BIPUSH, methodIndex);
            }
            methodIndex++;

            int numberOfParameters = method.getParameterTypes().length;

            int index = 0;
            int stack = 1;
            if (numberOfParameters == 0) {
                // no params, load null
                mv.visitInsn(Opcodes.ACONST_NULL);

            } else {
                // create the Object[] used to pass the parameters to _f3_invoke and push it on the stack
                if (numberOfParameters >= 0 && numberOfParameters <= 5) {
                    // use an integer constant if within range
                    mv.visitInsn(Opcodes.ICONST_0 + numberOfParameters);
                } else {
                    mv.visitIntInsn(Opcodes.BIPUSH, numberOfParameters);
                }
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                mv.visitInsn(DUP);

                for (Class<?> param : method.getParameterTypes()) {
                    if (Integer.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.ILOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                    } else if (Float.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.FLOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                    } else if (Boolean.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.ILOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                    } else if (Short.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.ILOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                    } else if (Byte.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.ILOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                    } else if (Double.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.DLOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                        stack++;   // double occupies two positions

                    } else if (Long.TYPE.equals(param)) {
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(Opcodes.LLOAD, stack);
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                        stack++;   // long occupies two positions
                    } else {
                        // object type
                        mv.visitInsn(Opcodes.ICONST_0 + index);
                        mv.visitVarInsn(ALOAD, stack);
                        mv.visitInsn(AASTORE);
                        if (index < numberOfParameters - 1) {
                            mv.visitInsn(DUP);
                        }
                    }
                    index++;
                }
                // TODO other primitive types
                stack++;
            }

            mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, "_f3_invoke", "(ILjava/lang/Object;)Ljava/lang/Object;");

            // handle return values
            writeReturn(method, label3, mv);

            // implement catch blocks
            index = 0;
            for (String exception : exceptions) {
                Label endLabel = exceptionLabels.get(index);
                mv.visitLabel(endLabel);
                mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{exception});
                mv.visitVarInsn(ASTORE, stack);
                Label label6 = new Label();
                mv.visitLabel(label6);
                mv.visitVarInsn(ALOAD, stack);
                mv.visitInsn(ATHROW);
                index++;
            }

            Label label7 = new Label();
            mv.visitLabel(label7);
            mv.visitMaxs(7, 5);
            mv.visitEnd();
        }

        cw.visitEnd();

        byte[] data = cw.toByteArray();
        Class<?> proxyClass = generationLoader.defineClass(className, data);
        try {
            return (T) proxyClass.newInstance();
        } catch (InstantiationException e) {
            throw new ProxyException(e);
        } catch (IllegalAccessException e) {
            throw new ProxyException(e);
        }
    }

    /**
     * Creates a proxy that sends a single parameter to a {@link ProxyDispatcher}.
     *
     * @param classLoaderKey the key for the classloader that the proxy interface is to be loaded in
     * @param interfaze      the proxy interface
     * @param methods        the proxy methods
     * @param dispatcher     the dispatcher
     * @return the proxy
     * @throws ProxyException if there is an error creating the proxy
     */
    @SuppressWarnings("unchecked")
    private <T> T createUnWrappedProxy(URI classLoaderKey, Class<T> interfaze, Method[] methods, Class<? extends ProxyDispatcher> dispatcher)
            throws ProxyException {

        String className = interfaze.getName() + "_Proxy_" + dispatcher.getSimpleName();  // ensure multiple dispatchers can be defined for the same interface

        // check if the proxy class has already been created
        BytecodeClassLoader generationLoader = getClassLoader(classLoaderKey);
        try {
            Class<T> proxyClass = (Class<T>) generationLoader.loadClass(className);
            return proxyClass.newInstance();
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (InstantiationException e) {
            throw new ProxyException(e);
        } catch (IllegalAccessException e) {
            throw new ProxyException(e);
        }

        String interfazeName = Type.getInternalName(interfaze);
        String handlerName = Type.getInternalName(dispatcher);
        String handlerDescriptor = Type.getDescriptor(dispatcher);
        String classNameInternal = Type.getInternalName(interfaze) + "_Proxy_" + dispatcher.getSimpleName();

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, classNameInternal, null, handlerName, new String[]{interfazeName});

        cw.visitSource(interfaze.getName() + "Proxy.java", null);

        // write the ctor
        writeConstructor(handlerName, handlerDescriptor, cw);

        // write the methods
        int methodIndex = 0;
        for (Method method : methods) {
            String methodSignature = Type.getMethodDescriptor(method);
            String[] exceptions = new String[method.getExceptionTypes().length];
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = Type.getInternalName(method.getExceptionTypes()[i]);
            }
            mv = cw.visitMethod(ACC_PUBLIC, method.getName(), methodSignature, null, exceptions);
            mv.visitCode();

            List<Label> exceptionLabels = new ArrayList<Label>();
            Label label2 = new Label();
            Label label3 = new Label();

            for (String exception : exceptions) {
                Label endLabel = new Label();
                exceptionLabels.add(endLabel);
                mv.visitTryCatchBlock(label2, label3, endLabel, exception);

            }

            mv.visitLabel(label2);
            mv.visitVarInsn(ALOAD, 0);

            // set the method index used to dispatch on
            if (methodIndex >= 0 && methodIndex <= 5) {
                // use an integer constant if within range
                mv.visitInsn(Opcodes.ICONST_0 + methodIndex);
            } else {
                mv.visitIntInsn(Opcodes.BIPUSH, methodIndex);
            }
            methodIndex++;

            int numberOfParameters = method.getParameterTypes().length;
            if (numberOfParameters > 1) {
                // FIXME
                throw new AssertionError("Not supported");
            }
            if (numberOfParameters == 0) {
                // no params, load null
                mv.visitInsn(Opcodes.ACONST_NULL);

            } else {

                Class<?> param = method.getParameterTypes()[0];
                if (Integer.TYPE.equals(param)) {
                    mv.visitVarInsn(ILOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                } else if (Float.TYPE.equals(param)) {
                    mv.visitVarInsn(Opcodes.FLOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                } else if (Boolean.TYPE.equals(param)) {
                    mv.visitVarInsn(Opcodes.ILOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                } else if (Short.TYPE.equals(param)) {
                    mv.visitVarInsn(Opcodes.ILOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                } else if (Byte.TYPE.equals(param)) {
                    mv.visitVarInsn(Opcodes.ILOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                } else if (Double.TYPE.equals(param)) {
                    mv.visitVarInsn(Opcodes.DLOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                } else if (Long.TYPE.equals(param)) {
                    mv.visitVarInsn(Opcodes.LLOAD, 1);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                } else {
                    // object type
                    mv.visitVarInsn(ALOAD, 0);
                }
            }

            mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, "_f3_invoke", "(ILjava/lang/Object;)Ljava/lang/Object;");

            // handle return values
            writeReturn(method, label3, mv);

            // implement catch blocks
            int index = 0;
            for (String exception : exceptions) {
                Label endLabel = exceptionLabels.get(index);
                mv.visitLabel(endLabel);
                mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{exception});
                mv.visitVarInsn(ASTORE, 1);
                Label label6 = new Label();
                mv.visitLabel(label6);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitInsn(ATHROW);
                index++;
            }

            Label label7 = new Label();
            mv.visitLabel(label7);
            mv.visitMaxs(7, 5);
            mv.visitEnd();
        }

        cw.visitEnd();

        byte[] data = cw.toByteArray();
        Class<?> proxyClass = generationLoader.defineClass(className, data);
        try {
            return (T) proxyClass.newInstance();
        } catch (InstantiationException e) {
            throw new ProxyException(e);
        } catch (IllegalAccessException e) {
            throw new ProxyException(e);
        }
    }

    private void writeConstructor(String handlerName, String handlerDescriptor, ClassWriter cw) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, handlerName, "<init>", "()V");
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", handlerDescriptor, null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void writeReturn(Method method, Label endLabel, MethodVisitor mv) {
        Class<?> returnType = method.getReturnType();

        if (Void.TYPE.equals(returnType)) {
            mv.visitInsn(Opcodes.POP);
            mv.visitLabel(endLabel);
            mv.visitInsn(RETURN);
        } else if (returnType.isPrimitive()) {
            if (Double.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.DRETURN);
            } else if (Long.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.LRETURN);
            } else if (Integer.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (Float.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.FRETURN);
            } else if (Short.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (Byte.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (Boolean.TYPE.equals(returnType)) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                mv.visitLabel(endLabel);
                mv.visitInsn(Opcodes.IRETURN);
            }
        } else {
            String internalTypeName = Type.getInternalName(returnType);
            mv.visitTypeInsn(CHECKCAST, internalTypeName);
            mv.visitLabel(endLabel);
            mv.visitInsn(ARETURN);
        }
    }

    /**
     * Returns a classloader for loading the proxy class, creating one if necessary.
     *
     * @param classLoaderKey the key of the contribution classloader the proxy is being loaded for; set as the parent
     * @return the classloader
     */
    private BytecodeClassLoader getClassLoader(URI classLoaderKey) {
        ClassLoader parent = classLoaderRegistry.getClassLoader(classLoaderKey);
        BytecodeClassLoader generationClassLoader = classLoaderCache.get(classLoaderKey);
        if (generationClassLoader == null) {
            generationClassLoader = new BytecodeClassLoader(classLoaderKey, parent);
            generationClassLoader.addParent(getClass().getClassLoader()); // proxy classes need to be visible as well
            classLoaderCache.put(classLoaderKey, generationClassLoader);
        }
        return generationClassLoader;
    }

}
