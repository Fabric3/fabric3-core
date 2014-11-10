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
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }



}
