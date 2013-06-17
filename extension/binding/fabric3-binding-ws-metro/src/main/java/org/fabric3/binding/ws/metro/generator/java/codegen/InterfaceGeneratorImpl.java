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
package org.fabric3.binding.ws.metro.generator.java.codegen;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;

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
            throws InterfaceGenerationException {
        if (!(interfaze.getClassLoader() instanceof SecureClassLoader)) {
            throw new InterfaceGenerationException("Classloader for " + interfaze.getName() + " must be a SecureClassLoader");
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
        } catch (IllegalAccessException e) {
            throw new InterfaceGenerationException(e);
        } catch (InvocationTargetException e) {
            throw new InterfaceGenerationException(e);
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