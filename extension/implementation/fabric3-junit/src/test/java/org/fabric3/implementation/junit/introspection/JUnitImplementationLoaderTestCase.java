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
package org.fabric3.implementation.junit.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.junit.model.JUnitBindingDefinition;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class JUnitImplementationLoaderTestCase extends TestCase {
    private static final String XML = "<junit class='org.fabric3.test.Foo'/>";
    private static final String CONFIGURATION_XML = "<junit class='org.fabric3.test.Foo'>" +
                                                    "   <configuration>" +
                                                    "      <username>username</username>" +
                                                    "      <password>password</password>" +
                                                    "   </configuration>" +
                                                    "</junit>";

    private JUnitImplementationLoader loader;

    public void testLoad() throws Exception {
        IntrospectionContext context = new DefaultIntrospectionContext();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(XML));
        reader.nextTag();
        JUnitImplementation definition = loader.load(reader, context);
        assertEquals("org.fabric3.test.Foo", definition.getImplementationClass());
    }

    public void testConfigurationLoad() throws Exception {
        IntrospectionContext context = new DefaultIntrospectionContext();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(CONFIGURATION_XML));
        reader.nextTag();
        JUnitImplementation definition = loader.load(reader, context);
        ServiceDefinition<ComponentType> serviceDefinition = definition.getComponentType().getServices().get("Foo");
        JUnitBindingDefinition bindingDefinition = (JUnitBindingDefinition) serviceDefinition.getBindings().get(0);
        assertEquals("username", bindingDefinition.getConfiguration().getUsername());
        assertEquals("password", bindingDefinition.getConfiguration().getPassword());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitImplementationIntrospector processor = EasyMock.createNiceMock(JUnitImplementationIntrospector.class);
        processor.introspect(EasyMock.isA(InjectingComponentType.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andAnswer(() -> {
            InjectingComponentType componentType = (InjectingComponentType) EasyMock.getCurrentArguments()[0];
            ServiceDefinition<ComponentType> serviceDefinition = new ServiceDefinition<>("Foo");
            serviceDefinition.setServiceContract(new JavaServiceContract() {
                private static final long serialVersionUID = 6696779955276690454L;

                public String getQualifiedInterfaceName() {
                    return "org.fabric3.test.Foo";
                }
            });
            componentType.add(serviceDefinition);
            return null;
        });
        EasyMock.replay(processor);
        loader = new JUnitImplementationLoader(processor);
    }
}
