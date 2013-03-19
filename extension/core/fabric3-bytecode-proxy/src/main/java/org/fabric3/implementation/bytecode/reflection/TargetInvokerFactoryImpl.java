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
package org.fabric3.implementation.bytecode.reflection;

import java.lang.reflect.Method;

import org.fabric3.implementation.bytecode.proxy.common.BytecodeClassLoader;
import org.fabric3.implementation.pojo.spi.reflection.TargetInvoker;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;

/**
 *
 */
public class TargetInvokerFactoryImpl implements TargetInvokerFactory {
    private static final String[] TARGET_INVOKER_INTERFACES = new String[]{Type.getInternalName(TargetInvoker.class)};
    private static final String[] EXCEPTIONS = new String[]{"java/lang/Exception"};

    @SuppressWarnings("unchecked")
    public TargetInvoker createTargetInvoker(Method method, BytecodeClassLoader classLoader) {
        Class<?> declaringClass = method.getDeclaringClass();

        // use the hashcode of the method since more than one invoker may be created per class (if it has multiple methods)
        int code = Math.abs(method.hashCode());
        String className = declaringClass.getName() + "_TargetInvoker" + code;

        try {
            Class<TargetInvoker> invokerClass = (Class<TargetInvoker>) classLoader.loadClass(className);
            return invokerClass.newInstance();
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        String internalTargetName = Type.getInternalName(declaringClass);
        String internalInvokerName = internalTargetName + "_TargetInvoker" + code;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, internalInvokerName, null, "java/lang/Object", TARGET_INVOKER_INTERFACES);

        cw.visitSource(className + ".java", null);

        // write the ctor
        BytecodeHelper.writeConstructor(cw, Object.class);

        // write the invoker method
        writeTargetInvoke(method, internalTargetName, cw);

        cw.visitEnd();

        return BytecodeHelper.instantiate(TargetInvoker.class, className, classLoader, cw);
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
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(method.getParameterTypes()[0]));
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

                if (Integer.TYPE.equals(paramType)) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");

                } else if (Double.TYPE.equals(paramType)) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");

                } else {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(paramType));
                }

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
        mv.visitInsn(Opcodes.ARETURN);
        Label label2 = new Label();
        mv.visitLabel(label2);
        String descriptor = Type.getDescriptor(TargetInvoker.class);

        mv.visitLocalVariable("this", descriptor, null, label1, label2, 0);
        mv.visitLocalVariable("instance", "Ljava/lang/Object;", null, label1, label2, 1);
        mv.visitLocalVariable("arg", "Ljava/lang/Object;", null, label1, label2, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }

}
