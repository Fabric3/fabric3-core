/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.w3c.dom.Document;

import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.spi.common.HeadersDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.OperationPropertiesDefinition;
import org.fabric3.model.type.PolicyAware;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Target;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidTargetException;
import org.fabric3.spi.introspection.xml.LoaderHelper;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class JMSBindingLoaderTestCase extends TestCase {
    public void testLoaderJMSBindingElement() throws Exception {
        LoaderHelper loaderHelper = new LoaderHelper() {

            public QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException {
                return null;
            }

            public URI parseUri(String target) {
                return null;
            }

            public Target parseTarget(String target, XMLStreamReader reader) throws InvalidTargetException {
                return null;
            }

            public String loadKey(XMLStreamReader reader) {
                return null;
            }

            public void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader, IntrospectionContext context) {
            }

            public Document loadPropertyValues(XMLStreamReader reader) throws XMLStreamException {
                return null;
            }

            public Document loadPropertyValue(String content) throws XMLStreamException {
                return null;
            }

            public Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException {
                return null;
            }

            public List<URI> parseListOfUris(XMLStreamReader reader, String attribute) {
                return null;
            }

            public boolean canNarrow(Multiplicity first, Multiplicity second) {
                return false;
            }

            public Document transform(XMLStreamReader reader) throws XMLStreamException {
                return null;
            }

        };
        JmsBindingLoader loader = new JmsBindingLoader(loaderHelper);
        InputStream inputStream = JmsBindingLoader.class.getResourceAsStream("JMSBindingLoaderTest.xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader streamReader = factory.createXMLStreamReader(new InputStreamReader(inputStream));
        loaderHelper.loadKey(streamReader);
        JmsBindingDefinition jmsBinding = null;
        while (streamReader.hasNext()) {
            if (START_ELEMENT == streamReader.next() && "binding.jms".equals(streamReader.getName().getLocalPart())) {
                jmsBinding = loader.load(streamReader, new DefaultIntrospectionContext());
                streamReader.close();
                break;
            }
        }
        JmsBindingMetadata metadata = jmsBinding.getJmsMetadata();
        HeadersDefinition headers = metadata.getHeaders();
        assertEquals("TestHeadersProperty", headers.getProperties().get("testHeadersProperty"));
        Map<String, OperationPropertiesDefinition> props = metadata.getOperationProperties();
        assertEquals(2, props.size());
        assertEquals("TestHeadersPropertyProperty", props.get("testOperationProperties1").getProperties().get("testHeadersPropertyProperty"));
        assertEquals("NestedHeader", props.get("testOperationProperties1").getHeaders().getProperties().get("nested"));
        assertEquals("NativeName", metadata.getOperationProperties().get("testOperationProperties2").getNativeOperation());
    }

}
