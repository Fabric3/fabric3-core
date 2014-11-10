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
package org.fabric3.implementation.junit.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.junit.common.ContextConfiguration;
import org.fabric3.implementation.junit.model.JUnitBindingDefinition;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.api.model.type.java.InjectingComponentType;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 */
@EagerInit
public class JUnitImplementationLoader extends AbstractValidatingTypeLoader<JUnitImplementation> {

    private final JUnitImplementationIntrospector introspector;

    public JUnitImplementationLoader(@Reference JUnitImplementationIntrospector introspector) {
        this.introspector = introspector;
        addAttributes("class", "requires", "policySets");
    }

    public JUnitImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String className = reader.getAttributeValue(null, "class");
        JUnitImplementation implementation = new JUnitImplementation(className);

        validateAttributes(reader, context, implementation);

        InjectingComponentType componentType = new InjectingComponentType(className);
        introspector.introspect(componentType, context);
        implementation.setComponentType(componentType);

        // Add a binding only on the JUnit service (which is the impl class) so wires are generated to the test operations.
        // These wires will be used by the testing runtime to dispatch to the JUnit components.
        ContextConfiguration configuration = loadConfiguration(reader, implementation, context);
        for (AbstractService serviceDefinition : implementation.getComponentType().getServices().values()) {
            if (serviceDefinition.getServiceContract().getQualifiedInterfaceName().equals(implementation.getImplementationClass())) {
                JUnitBindingDefinition bindingDefinition = new JUnitBindingDefinition(configuration);
                serviceDefinition.addBinding(bindingDefinition);
                break;
            }
        }
        return implementation;
    }

    private ContextConfiguration loadConfiguration(XMLStreamReader reader, JUnitImplementation implementation, IntrospectionContext context)
            throws XMLStreamException {
        ContextConfiguration configuration = null;
        String name;
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    Location startLocation = reader.getLocation();

                    name = reader.getName().getLocalPart();
                    if ("configuration".equals(name)) {
                        configuration = new ContextConfiguration();
                    }
                    if ("username".equals(name)) {
                        if (configuration == null) {
                            InvalidContextConfiguration error = new InvalidContextConfiguration(
                                    "Username element must be contained within a configuration element",
                                    startLocation,
                                    implementation);
                            context.addError(error);
                        } else {
                            configuration.setUsername(reader.getElementText());
                        }
                    } else if ("password".equals(name)) {
                        if (configuration == null) {
                            InvalidContextConfiguration error = new InvalidContextConfiguration(
                                    "Password element must be contained within a configuration element",
                                    startLocation,
                                    implementation);
                            context.addError(error);
                        } else {
                            configuration.setPassword(reader.getElementText());
                        }
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("junit".equals(name)) {
                        return configuration;
                    }
                    break;
            }
        }
    }

}
