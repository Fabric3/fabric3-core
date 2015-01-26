/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.bytecode.reflection;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Names;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.spi.classloader.BytecodeClassLoader;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.oasisopen.sca.annotation.Reference;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 *
 */
public class BytecodeConsumerInvokerFactory implements ConsumerInvokerFactory, ContributionServiceListener {
    private static final String[] TARGET_INVOKER_INTERFACES = new String[]{Type.getInternalName(ConsumerInvoker.class)};
    private static final String[] EXCEPTIONS = new String[]{"java/lang/Exception"};

    private ClassLoaderRegistry classLoaderRegistry;

    private Map<URI, BytecodeClassLoader> classLoaderCache = new HashMap<>();

    public BytecodeConsumerInvokerFactory(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void onUninstall(Contribution contribution) {
        // remove cached classloader for the contribution on undeploy
        classLoaderCache.remove(contribution.getUri());
    }

    public boolean isDefault() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public ConsumerInvoker createInvoker(Method method) {
        BytecodeClassLoader classLoader = getClassLoader(method);

        Class<?> declaringClass = method.getDeclaringClass();

        // use the toString() hashcode of the method since more than one invoker may be created per class (if it has multiple methods)
        int code = Math.abs(method.toString().hashCode());
        String className = declaringClass.getName() + "_ConsumerInvoker" + code;

        try {
            Class<ConsumerInvoker> invokerClass = (Class<ConsumerInvoker>) classLoader.loadClass(className);
            return invokerClass.newInstance();
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError(e);
        }

        String internalTargetName = Type.getInternalName(declaringClass);
        String internalInvokerName = internalTargetName + "_ConsumerInvoker" + code;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, internalInvokerName, null, "java/lang/Object", TARGET_INVOKER_INTERFACES);

        cw.visitSource(className + ".java", null);

        // write the ctor
        BytecodeHelper.writeConstructor(cw, Object.class);

        // write the invoker method
        writeTargetInvoke(method, internalTargetName, cw);

        cw.visitEnd();

        return BytecodeHelper.instantiate(ConsumerInvoker.class, className, classLoader, cw);
    }

    private void writeTargetInvoke(Method method, String internalTargetName, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, EXCEPTIONS);
        mv.visitCode();
        Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitLineNumber(9, label1);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, internalTargetName);

        if (method.getParameterTypes().length == 1) {
            // single argument method, load the parameter passes on to the stack
            Class<?> paramType = method.getParameterTypes()[0];
            mv.visitVarInsn(Opcodes.ALOAD, 2);

            writeParam(paramType, mv);

        } else if (method.getParameterTypes().length > 1) {
            // multi-argument method: cast the parameter to an object array and then load each element on the stack to be passed as params
            mv.visitVarInsn(Opcodes.ALOAD, 2);

            mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            int pos = 0;
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            for (Class<?> paramType : method.getParameterTypes()) {
                mv.visitInsn(Opcodes.ICONST_0 + pos);
                mv.visitInsn(Opcodes.AALOAD);

                writeParam(paramType, mv);

                if (pos < method.getParameterTypes().length - 1) {
                    mv.visitVarInsn(Opcodes.ALOAD, 3);
                }
                pos++;
            }
        }

        // invoke the instance
        String methodName = method.getName();
        String methodDescriptor = Type.getMethodDescriptor(method);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalTargetName, methodName, methodDescriptor);

        Class<?> returnType = method.getReturnType();
        writeReturn(returnType, mv);

        Label label2 = new Label();
        mv.visitLabel(label2);
        String descriptor = Type.getDescriptor(ServiceInvoker.class);

        mv.visitLocalVariable("this", descriptor, null, label1, label2, 0);
        mv.visitLocalVariable("instance", "Ljava/lang/Object;", null, label1, label2, 1);
        mv.visitLocalVariable("arg", "Ljava/lang/Object;", null, label1, label2, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }

    private void writeReturn(Class<?> returnType, MethodVisitor mv) {
        if (Void.TYPE.equals(returnType)) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        } else if (returnType.isPrimitive()) {
            if (Integer.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                mv.visitInsn(Opcodes.ARETURN);
            } else if (Boolean.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                mv.visitInsn(Opcodes.ARETURN);
            } else if (Double.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                mv.visitInsn(Opcodes.ARETURN);
            } else if (Long.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                mv.visitInsn(Opcodes.ARETURN);
            } else if (Float.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                mv.visitInsn(Opcodes.ARETURN);
            } else if (Short.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                mv.visitInsn(Opcodes.ARETURN);
            } else if (Byte.TYPE.equals(returnType)) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                mv.visitInsn(Opcodes.ARETURN);
            }
        } else {
            mv.visitInsn(Opcodes.ARETURN);
        }
    }

    private void writeParam(Class<?> paramType, MethodVisitor mv) {
        if (Integer.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
        } else if (Boolean.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
        } else if (Double.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
        } else if (Float.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
        } else if (Short.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
        } else if (Byte.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
        } else if (Long.TYPE.equals(paramType)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
        }
    }

    /**
     * Returns a classloader for loading the proxy class, creating one if necessary.
     *
     * @return the classloader
     */
    private BytecodeClassLoader getClassLoader(Member method) {

        URI classLoaderKey;
        ClassLoader classLoader = method.getDeclaringClass().getClassLoader();
        if (classLoader instanceof MultiParentClassLoader) {
            classLoaderKey = ((MultiParentClassLoader) classLoader).getName();
        } else {
            classLoaderKey = Names.BOOT_CONTRIBUTION;
        }

        ClassLoader parent = classLoaderRegistry.getClassLoader(classLoaderKey);
        BytecodeClassLoader generationClassLoader = classLoaderCache.get(classLoaderKey);
        if (generationClassLoader == null) {
            generationClassLoader = new BytecodeClassLoader(classLoaderKey, parent);
            generationClassLoader.addParent(getClass().getClassLoader()); // SPI classes need to be visible as well
            classLoaderCache.put(classLoaderKey, generationClassLoader);
        }
        return generationClassLoader;
    }

    public void onStore(Contribution contribution) {

    }

    public void onProcessManifest(Contribution contribution) {

    }

    public void onInstall(Contribution contribution) {

    }

    public void onUpdate(Contribution contribution) {

    }

    public void onRemove(Contribution contribution) {

    }
}
