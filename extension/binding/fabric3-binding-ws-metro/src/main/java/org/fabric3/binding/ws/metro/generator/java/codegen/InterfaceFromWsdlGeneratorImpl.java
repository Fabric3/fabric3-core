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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;
import java.util.Collection;
import java.util.List;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.util.ClassDefiner;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.fabric3.binding.ws.metro.generator.java.codegen.GeneratorHelper.getSignature;

/**
 *
 */
@Service(InterfaceFromWsdlGenerator.class)
public class InterfaceFromWsdlGeneratorImpl implements InterfaceFromWsdlGenerator, Opcodes {
    private static final String SUFFIX = "F3Subtype";

    private ClassDefiner definer;

    public InterfaceFromWsdlGeneratorImpl(@Reference ClassDefiner definer) {
        this.definer = definer;
    }

    public boolean doGeneration(Class<?> clazz) {
        return !clazz.isAnnotationPresent(WebService.class);
    }

    public GeneratedInterface generateRPCLit(Class interfaze, ReferenceEndpointDefinition endpointDefinition) throws InterfaceGenerationException {

        if (!(interfaze.getClassLoader() instanceof SecureClassLoader)) {
            throw new InterfaceGenerationException("Classloader for " + interfaze.getName() + " must be a SecureClassLoader");
        }
        SecureClassLoader loader = (SecureClassLoader) interfaze.getClassLoader();
        String name = interfaze.getName();
        String internalName = name.replace('.', '/');
        String generatedInternalName = internalName + SUFFIX;
        ClassWriter cw = new ClassWriter(0);
        byte[] bytes = generate(cw, generatedInternalName, interfaze, endpointDefinition);
        String generatedName = name + SUFFIX;

        try {
            Class<?> clazz = definer.defineClass(generatedName, bytes, loader);
            return new GeneratedInterface(clazz, bytes);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InterfaceGenerationException(e);
        }

    }

    private byte[] generate(ClassWriter cw, String className, Class<?> clazz, ReferenceEndpointDefinition endpointDefinition) {
        String[] interfaces = {clazz.getName().replace('.', '/')};
        cw.visit(V1_5, ACC_INTERFACE | ACC_PUBLIC, className, null, "java/lang/Object", interfaces);
        endpointDefinition.getPortTypeName();
        Definition definition = endpointDefinition.getDefinition();
        if (!clazz.isAnnotationPresent(WebService.class)) {
            // add @WebService if it is not present
            AnnotationVisitor av = cw.visitAnnotation(getSignature(WebService.class), true);
            // Set the port type name attribute to the original class name. This corresponds to Java-to-WSDL mappings as defined in
            // the JAX-WS specification (section 3.11)
            av.visit("targetNamespace", definition.getTargetNamespace());

            javax.wsdl.Service service = (javax.wsdl.Service) definition.getServices().values().iterator().next();
            av.visit("serviceName", service.getQName().getLocalPart());
            Port port = (Port) service.getPorts().values().iterator().next();
            av.visit("portName", port.getName());
            av.visitEnd();

            av = cw.visitAnnotation(getSignature(SOAPBinding.class), true);
            av.visitEnum("style", getSignature(SOAPBinding.Style.class), "RPC");
            av.visitEnd();

        }
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            generateMethod(cw, m, definition, endpointDefinition);
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private void generateMethod(ClassWriter cw, Method m, Definition definition, ReferenceEndpointDefinition endpointDefinition) {
        MethodVisitor mv;

        QName portTypeName = endpointDefinition.getPortTypeName();
        Binding binding = null;
        Collection<Binding> bindings = definition.getBindings().values();
        for (Binding entry : bindings) {
            if (entry.getPortType().getQName().equals(portTypeName)) {
                binding = entry;
                break;
            }
        }

        if (binding == null) {
            throw new AssertionError();
        }

        BindingOperation bindingOperation = null;
        List<BindingOperation> bindingOperations = binding.getBindingOperations();
        for (BindingOperation operation : bindingOperations) {
            if (operation.getName().equals(m.getName())) {
                bindingOperation = operation;
                break;
            }
        }

        SOAPOperation soapOperation = null;
        for (Object element : bindingOperation.getExtensibilityElements()) {
            if (element instanceof SOAPOperation) {
                soapOperation = (SOAPOperation) element;
            }
        }
        String action = soapOperation.getSoapActionURI();

        String signature = getSignature(m);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, m.getName(), signature, null, null);

        AnnotationVisitor av = mv.visitAnnotation(getSignature(WebMethod.class), true);
        av.visit("action", action);
        av.visitEnd();

        PortType portType = definition.getPortType(portTypeName);
        List<Operation> portTypeOperations = portType.getOperations();
        Operation portTypeOperation = null;
        for (Operation entry : portTypeOperations) {
            if (entry.getName().equals(m.getName())) {
                portTypeOperation = entry;
            }
        }
        av = mv.visitAnnotation(getSignature(WebResult.class), true);
        av.visit("name", portTypeOperation.getOutput().getMessage().getQName().getLocalPart());
        Collection<Part> parts = portTypeOperation.getOutput().getMessage().getParts().values();
        av.visit("partName", parts.iterator().next().getName());
        av.visitEnd();

        av = mv.visitParameterAnnotation(0, getSignature(WebParam.class), true);
        parts = portTypeOperation.getInput().getMessage().getParts().values();
        String partName = parts.iterator().next().getName();
        av.visit("name", partName);
        av.visit("partName", partName);
        av.visitEnd();
        mv.visitEnd();
    }

}