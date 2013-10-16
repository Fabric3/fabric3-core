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
package org.fabric3.wsdl.factory.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.w3c.dom.Element;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.wsdl.contribution.impl.SourceWsdlLocator;

/**
 *
 */
public class Wsdl4JFactoryImplTestCase extends TestCase {
    private static QName ELEMENT_TYPE = new QName("urn:mock", "mockExtensibilityElement");

    public void testRegistration() throws Exception {
        Wsdl4JFactoryImpl factory = new Wsdl4JFactoryImpl();
        factory.register(Binding.class, ELEMENT_TYPE, MockExtensibilityElement.class, new MockSerializer(), new MockDeserializer());
        WSDLReader reader = factory.newReader();
        Definition definition = reader.readWSDL(getClass().getResource("extensionTest.wsdl").toURI().toString());
        Binding binding = definition.getBinding(new QName("http://example.com/stockquote.wsdl", "StockQuoteSoapBinding"));

        boolean found = findElement(binding);
        assertTrue("Extensibility element not added", found);

        factory.unregister(Binding.class, ELEMENT_TYPE, MockExtensibilityElement.class);
        reader = factory.newReader();
        definition = reader.readWSDL(getClass().getResource("extensionTest.wsdl").toURI().toString());
        binding = definition.getBinding(new QName("http://example.com/stockquote.wsdl", "StockQuoteSoapBinding"));

        found = findElement(binding);
        assertTrue("Extensibility should not be added", !found);

    }

    public void testWriter() throws Exception {
        Wsdl4JFactoryImpl factory = new Wsdl4JFactoryImpl();
        factory.register(Binding.class, ELEMENT_TYPE, MockExtensibilityElement.class, new MockSerializer(), new MockDeserializer());
        WSDLReader reader = factory.newReader();
        Definition definition = reader.readWSDL(getClass().getResource("extensionTest.wsdl").toURI().toString());
        WSDLWriter writer = factory.newWriter();
        StringWriter stringWriter = new StringWriter();
        writer.writeWSDL(definition, stringWriter);
        assertTrue(stringWriter.getBuffer().toString().contains("mockExtensibilityElement"));

    }
    
    public void testLocator() throws Exception {
        Wsdl4JFactoryImpl factory = new Wsdl4JFactoryImpl();
        WSDLReader reader = factory.newReader();
        
        Source urlSource = new UrlSource(getClass().getResource("locatorTest.wsdl"));
        IntrospectionContext context = new DefaultIntrospectionContext();
        WSDLLocator locator = new SourceWsdlLocator(urlSource, context);
        Definition definition = reader.readWSDL(locator);
        assertEquals(0, context.getErrors().size());
        assertNotNull(definition);
        
        Service service = definition.getService(new QName("http://example.com/locatorTest.wsdl", "TestImportService"));
        assertNotNull(service);
        QName portType = service.getPort("TestImportPort").getBinding().getPortType().getQName();
        assertEquals(new QName("http://example.com/stockquote.wsdl", "StockQuotePortType"), portType);
    }

    private boolean findElement(Binding binding) {
        boolean found = false;
        for (Object element : binding.getExtensibilityElements()) {
            if (element instanceof MockExtensibilityElement) {
                found = true;
                break;
            }
        }
        return found;
    }

    private class MockDeserializer implements ExtensionDeserializer {

        public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el, Definition def, ExtensionRegistry extReg)
                throws WSDLException {
            return new MockExtensibilityElement();
        }
    }

    private class MockSerializer implements ExtensionSerializer {
        public void marshall(Class parentType,
                             QName elementType,
                             ExtensibilityElement extension,
                             PrintWriter pw,
                             Definition def,
                             ExtensionRegistry extReg) throws WSDLException {
            pw.write("<mockExtensibilityElement/>");

        }
    }

    private class MockExtensibilityElement implements ExtensibilityElement {

        public void setElementType(QName elementType) {
            throw new UnsupportedOperationException();
        }

        public QName getElementType() {
            return ELEMENT_TYPE;
        }

        public void setRequired(Boolean required) {
            throw new UnsupportedOperationException();
        }

        public Boolean getRequired() {
            return false;
        }
    }
}