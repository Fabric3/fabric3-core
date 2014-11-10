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
package org.fabric3.wsdl.contribution.impl;

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
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.w3c.dom.Element;

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