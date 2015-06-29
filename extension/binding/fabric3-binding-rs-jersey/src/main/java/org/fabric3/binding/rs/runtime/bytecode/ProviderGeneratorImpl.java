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
 */
package org.fabric3.binding.rs.runtime.bytecode;

import javax.annotation.Priority;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.spi.classloader.BytecodeClassLoader;
import org.fabric3.spi.introspection.java.AnnotationHelper;
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

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> generate(Class<T> baseClass, Class<?> delegateClass, String genericSignature) {
        ClassWriter cw = new ClassWriter(0);
        int number = counter.getAndIncrement();

        byte[] bytes = writeClass(cw, baseClass, delegateClass, genericSignature, number);

        String generatedName = baseClass.getName() + SUFFIX + "_" + number;
        BytecodeClassLoader bytecodeClassLoader = new BytecodeClassLoader(URI.create("BytecodeClassLoader"), getClass().getClassLoader());
        bytecodeClassLoader.addParent(delegateClass.getClassLoader());

        return (Class<? extends T>) bytecodeClassLoader.defineClass(generatedName, bytes);
    }

    private byte[] writeClass(ClassWriter cw, Class<?> baseClass, Class<?> delegateClass, String genericSignature, int number) {
        String internalName = Type.getInternalName(baseClass);
        String generatedInternalName = internalName + SUFFIX + "_" + number;
        String descriptor = Type.getDescriptor(baseClass);

        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, generatedInternalName, genericSignature, internalName, null);
        writeAnnotations(cw, delegateClass);
        writeConstructor(internalName, descriptor, cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private void writeAnnotations(ClassWriter cw, Class<?> delegateClass) {
        Provider provider = AnnotationHelper.findAnnotation(Provider.class, delegateClass);
        if (provider != null) {
            AnnotationVisitor av = cw.visitAnnotation(getSignature(Provider.class), true);
            av.visitEnd();
        }
        Priority priority =  AnnotationHelper.findAnnotation(Priority.class, delegateClass);
        if (priority != null) {
            AnnotationVisitor av = cw.visitAnnotation(getSignature(Priority.class), true);
            av.visit("value", priority.value());
            av.visitEnd();
        }

        Produces produces =  AnnotationHelper.findAnnotation(Produces.class, delegateClass);
        if (produces != null) {
            AnnotationVisitor av = cw.visitAnnotation(getSignature(Produces.class), true);
            AnnotationVisitor arrayVisitor = av.visitArray("value");
            for (String entry : produces.value()) {
                arrayVisitor.visit("value", entry);
            }
            arrayVisitor.visitEnd();
            av.visitEnd();
        }

        Consumes consumes =  AnnotationHelper.findAnnotation(Consumes.class, delegateClass);
        if (consumes != null) {
            AnnotationVisitor av = cw.visitAnnotation(getSignature(Consumes.class), true);
            AnnotationVisitor arrayVisitor = av.visitArray("value");
            for (String entry : consumes.value()) {
                arrayVisitor.visit("value", entry);
            }
            arrayVisitor.visitEnd();
            av.visitEnd();
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
