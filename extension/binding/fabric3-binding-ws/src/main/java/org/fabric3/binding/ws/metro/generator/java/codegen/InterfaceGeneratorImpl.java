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
package org.fabric3.binding.ws.metro.generator.java.codegen;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;

import org.fabric3.api.host.ContainerException;
import org.fabric3.binding.ws.metro.util.ClassDefiner;
import org.oasisopen.sca.annotation.OneWay;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.fabric3.binding.ws.metro.generator.java.codegen.GeneratorHelper.getSignature;

/**
 * Default implementation of InterfaceGenerator that uses ASM to generate a subclass of the original type with JAX-WS annotations.
 */
@Service(InterfaceGenerator.class)
public class InterfaceGeneratorImpl implements InterfaceGenerator, Opcodes {
    private static final String SUFFIX = "F3Subtype";

    private ClassDefiner definer;

    public InterfaceGeneratorImpl(@Reference ClassDefiner definer) {
        this.definer = definer;
    }

    public boolean doGeneration(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(WebService.class)) {
            // @WebService is required by Metro
            return true;
        }
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(OneWay.class)) {
                return true;
            }
        }
        return false;
    }

    public GeneratedInterface generate(Class interfaze, String targetNamespace, String wsdlLocation, String serviceName, String portName)
            throws ContainerException {
        if (!(interfaze.getClassLoader() instanceof SecureClassLoader)) {
            throw new ContainerException("Classloader for " + interfaze.getName() + " must be a SecureClassLoader");
        }
        SecureClassLoader loader = (SecureClassLoader) interfaze.getClassLoader();
        String name = interfaze.getName();
        String internalName = name.replace('.', '/');
        String generatedInternalName = internalName + SUFFIX;
        ClassWriter cw = new ClassWriter(0);
        byte[] bytes = generate(cw, generatedInternalName, interfaze, targetNamespace, wsdlLocation, serviceName, portName);
        String generatedName = name + SUFFIX;

        try {
            Class<?> clazz = definer.defineClass(generatedName, bytes, loader);
            return new GeneratedInterface(clazz, bytes);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ContainerException(e);
        }
    }

   private byte[] generate(ClassWriter cw,
                            String className,
                            Class<?> clazz,
                            String targetNamespace,
                            String wsdlLocation,
                            String serviceName,
                            String portName) {
        String[] interfaces = {clazz.getName().replace('.', '/')};
        cw.visit(V1_5, ACC_INTERFACE | ACC_PUBLIC, className, null, "java/lang/Object", interfaces);

        if (!clazz.isAnnotationPresent(WebService.class)) {
            // add @WebService if it is not present
            AnnotationVisitor av = cw.visitAnnotation(getSignature(WebService.class), true);
            // Set the port type name attribute to the original class name. This corresponds to Java-to-WSDL mappings as defined in
            // the JAX-WS specification (section 3.11)
            av.visit("name", clazz.getSimpleName());
            if (targetNamespace != null) {
                av.visit("targetNamespace", targetNamespace);
            }
            if (wsdlLocation != null) {
                av.visit("wsdlLocation", wsdlLocation);
            }
            if (serviceName != null) {
                av.visit("serviceName", serviceName);
            }
            if (portName != null) {
                av.visit("portName", portName);
            }
            av.visitEnd();
        }
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            generateMethod(cw, m);
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private void generateMethod(ClassWriter cw, Method m) {
        MethodVisitor mv;
        String signature = getSignature(m);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, m.getName(), signature, null, null);

        if (!m.isAnnotationPresent(WebMethod.class)) {
            // add @WebMethod if it is not present
            AnnotationVisitor av = mv.visitAnnotation(getSignature(WebMethod.class), true);
            av.visitEnd();
        }
        if (!m.isAnnotationPresent(Oneway.class)) {
            if (m.isAnnotationPresent(OneWay.class)) {
                // add the JAX-WS one-way equivalent
                AnnotationVisitor oneWay = mv.visitAnnotation(getSignature(Oneway.class), true);
                oneWay.visitEnd();
            }
        }
        mv.visitEnd();
    }



}