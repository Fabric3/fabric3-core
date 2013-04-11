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

import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.spi.classloader.BytecodeClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 *
 */
public class BytecodeHelper {

    /**
     * Creates a no-args constructor.
     *
     * @param cw the class writer
     */
    public static void writeConstructor(ClassWriter cw, Class<?> superType) {
        String descriptor = Type.getDescriptor(ServiceInvoker.class);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label label = new Label();
        mv.visitLabel(label);
        mv.visitLineNumber(6, label);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(superType), "<init>", "()V");
        mv.visitInsn(Opcodes.RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", descriptor, null, label, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * Instantiates an instance of the class.
     *
     * @param type        the expected type
     * @param className   the class name
     * @param classLoader the classloader defining the class
     * @param cw          the class writer
     * @return the instance
     */
    public static <T> T instantiate(Class<T> type, String className, BytecodeClassLoader classLoader, ClassWriter cw) {
        byte[] data = cw.toByteArray();
        Class<?> invokerClass = classLoader.defineClass(className, data);
        try {
            return type.cast(invokerClass.newInstance());
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }



}
