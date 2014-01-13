/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.binding.rs.runtime.bytecode;

import javax.annotation.Priority;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.spi.classloader.BytecodeClassLoader;
import org.oasisopen.sca.annotation.Init;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 *
 */
public class ProviderGeneratorImpl implements ProviderGenerator {
    private static final String SUFFIX = "F3Subtype";
    private AtomicInteger counter = new AtomicInteger(1);

    private BytecodeClassLoader bytecodeClassLoader;

    @Init
    public void init() {
        bytecodeClassLoader = new BytecodeClassLoader(URI.create("BytecodeClassLoader"), getClass().getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> generate(Class<T> baseClass, Class<?> delegateClass) {
        ClassWriter cw = new ClassWriter(0);
        int number = counter.getAndIncrement();

        byte[] bytes = writeClass(cw, baseClass, delegateClass, number);

        String generatedName = baseClass.getName() + SUFFIX + "_" + number;
        return (Class<? extends T>) bytecodeClassLoader.defineClass(generatedName, bytes);
    }

    private byte[] writeClass(ClassWriter cw, Class<?> baseClass, Class<?> delegateClass, int number) {
        String internalName = Type.getInternalName(baseClass);
        String generatedInternalName = internalName + SUFFIX + "_" + number;
        String descriptor = Type.getDescriptor(baseClass);

        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, generatedInternalName, null, internalName, null);
        writeAnnotations(cw, delegateClass);
        writeConstructor(internalName, descriptor, cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private void writeAnnotations(ClassWriter cw, Class<?> delegateClass) {
        Priority priority = delegateClass.getAnnotation(Priority.class);
        if (priority == null) {
            return;
        }

        AnnotationVisitor av = cw.visitAnnotation(getSignature(Priority.class), true);
        av.visit("value", priority.value());
        av.visitEnd();
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

    public String getSignature(Class clazz) {
        if (clazz == Void.TYPE) {
            return "V";
        }
        if (clazz == Byte.TYPE) {
            return "B";
        } else if (clazz == Character.TYPE) {
            return "C";
        } else if (clazz == Double.TYPE) {
            return "D";
        } else if (clazz == Float.TYPE) {
            return "F";
        } else if (clazz == Integer.TYPE) {
            return "I";
        } else if (clazz == Long.TYPE) {
            return "J";
        } else if (clazz == Short.TYPE) {
            return "S";
        } else if (clazz == Boolean.TYPE) {
            return "Z";
        } else if (!clazz.getName().startsWith("[")) {
            // object
            return "L" + clazz.getName().replace('.', '/') + ";";
        } else {
            // array
            return clazz.getName().replace('.', '/');
        }
    }

}
