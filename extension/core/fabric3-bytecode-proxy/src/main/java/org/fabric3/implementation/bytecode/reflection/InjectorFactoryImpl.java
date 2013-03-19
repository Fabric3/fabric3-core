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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.fabric3.implementation.bytecode.proxy.common.BytecodeClassLoader;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectFactory;
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
public class InjectorFactoryImpl implements InjectorFactory {

    @SuppressWarnings("unchecked")
    public BytecodeInjector createInjector(Member member, ObjectFactory<?> parameterFactory, BytecodeClassLoader classLoader) {

        Class<?> declaringClass = member.getDeclaringClass();

        // use the hashcode of the method since more than one invoker may be created per class (if it has multiple methods)
        int code = Math.abs(member.hashCode());
        String className = declaringClass.getName() + "_Injector" + code;

        // check if the proxy class has already been created
        try {
            Class<Injector<?>> injectorClass = (Class<Injector<?>>) classLoader.loadClass(className);
            BytecodeInjector injector = (BytecodeInjector) injectorClass.newInstance();
            injector.init(parameterFactory);
            return injector;
        } catch (ClassNotFoundException e) {
            // ignore
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        String internalTargetName = Type.getInternalName(declaringClass);
        String internalInvokerName = internalTargetName + "_Injector" + code;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cw.visit(Opcodes.V1_7, ACC_PUBLIC + ACC_SUPER, internalInvokerName, null, Type.getInternalName(BytecodeInjector.class), null);

        cw.visitSource(className + ".java", null);

        // write the ctor
        BytecodeHelper.writeConstructor(cw, BytecodeInjector.class);

        writeInject(member, internalTargetName, cw);

        cw.visitEnd();

        BytecodeInjector injector = BytecodeHelper.instantiate(BytecodeInjector.class, className, classLoader, cw);
        injector.init(parameterFactory);
        return injector;
    }

    private void writeInject(Member member, String internalTargetName, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "inject", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(9, l0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, internalTargetName);
        mv.visitVarInsn(Opcodes.ALOAD, 2);

        if (member instanceof Field) {
            Field field = (Field) member;
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(field.getType()));
            mv.visitFieldInsn(Opcodes.PUTFIELD, internalTargetName, field.getName(), Type.getDescriptor(field.getType()));
        } else if (member instanceof Method) {
            Method method = (Method) member;
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(method.getParameterTypes()[0]));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalTargetName, method.getName(), Type.getMethodDescriptor(method));
        } else {
            throw new AssertionError("Unsupported type: " + member.getClass().getName());
        }
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLineNumber(10, l1);
        mv.visitInsn(Opcodes.RETURN);

        String descriptor = Type.getDescriptor(Injector.class);

        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLocalVariable("this", descriptor, null, l0, l2, 0);
        mv.visitLocalVariable("instance", "Ljava/lang/Object;", null, l0, l2, 1);
        mv.visitLocalVariable("target", "Ljava/lang/Object;", null, l0, l2, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();

    }
}
