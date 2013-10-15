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
package org.fabric3.implementation.junit.introspection;

import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.implementation.junit.model.JUnitBindingDefinition;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.model.type.component.AbstractService;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class JUnitImplementationLoaderTestCase extends TestCase {
    private static final String XML = "<junit class='org.fabric3.test.Foo'/>";
    private static final String CONFIGURATION_XML =
            "<junit class='org.fabric3.test.Foo'>" +
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
        AbstractService serviceDefinition = definition.getComponentType().getServices().get("Foo");
        JUnitBindingDefinition bindingDefinition = (JUnitBindingDefinition) serviceDefinition.getBindings().get(0);
        assertEquals("username", bindingDefinition.getConfiguration().getUsername());
        assertEquals("password", bindingDefinition.getConfiguration().getPassword());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitImplementationIntrospector processor = EasyMock.createNiceMock(JUnitImplementationIntrospector.class);
        processor.introspect(EasyMock.isA(String.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                InjectingComponentType componentType = new InjectingComponentType();
                ServiceDefinition serviceDefinition = new ServiceDefinition("Foo");
                serviceDefinition.setServiceContract(new JavaServiceContract() {
                    private static final long serialVersionUID = 6696779955276690454L;

                    public String getQualifiedInterfaceName() {
                        return "org.fabric3.test.Foo";
                    }
                });
                componentType.add(serviceDefinition);
                return componentType;
            }
        });
        EasyMock.replay(processor);
        loader = new JUnitImplementationLoader(processor);
    }
}
