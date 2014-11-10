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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.common;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.ResourceNotFound;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a Java interface definition from an XML-based assembly file
 */
public class JavaInterfaceLoader extends AbstractValidatingTypeLoader<ServiceContract> {

    private final JavaContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public JavaInterfaceLoader(@Reference JavaContractProcessor contractProcessor,
                               @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
        addAttributes("interface", "callbackInterface");
    }

    public ServiceContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "interface");
        if (name == null) {
            JavaServiceContract contract = new JavaServiceContract();
            MissingAttribute failure = new MissingAttribute("An interface must be specified using the class attribute", startLocation, contract);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return contract;
        }
        Class<?> interfaceClass;
        try {
            interfaceClass = helper.loadClass(name, context.getClassLoader());
        } catch (ImplementationNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Interface not found: " + name, startLocation);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return null;
        }

        ServiceContract serviceContract = contractProcessor.introspect(interfaceClass, context);

        validateAttributes(reader, context, serviceContract);

        name = reader.getAttributeValue(null, "callbackInterface");

        Class<?> callbackClass;
        try {
            callbackClass = (name != null) ? helper.loadClass(name, context.getClassLoader()) : null;
        } catch (ImplementationNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Callback interface not found: " + name, startLocation, serviceContract);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return serviceContract;
        }


        LoaderUtil.skipToEndElement(reader);

        TypeMapping mapping = context.getTypeMapping(interfaceClass);
        if (mapping == null) {
            mapping = new TypeMapping();
            context.addTypeMapping(interfaceClass, mapping);
        }
        helper.resolveTypeParameters(interfaceClass, mapping);
        if (callbackClass != null) {
            helper.resolveTypeParameters(callbackClass, mapping);
            ServiceContract callbackContract = contractProcessor.introspect(callbackClass, context);
            serviceContract.setCallbackContract(callbackContract);
        }
        return serviceContract;
    }

}
