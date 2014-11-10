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
package org.fabric3.wsdl.processor;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Element;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 *
 */
public class Wsdl11ContractProcessorTestCase extends TestCase {
    private static final QName STOCK_QUOTE_PORT_TYPE = new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");
    private WsdlContractProcessor processor;
    private PortType portType;
    private XmlSchemaCollection schemaCollection;
    private Definition definition;

    @SuppressWarnings({"unchecked"})
    public void testIntrospect() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        WsdlServiceContract contract = processor.introspect(portType, definition, schemaCollection, context);

        assertEquals(1, contract.getOperations().size());
        Operation operation = contract.getOperations().get(0);
        assertEquals("GetLastTradePrice", operation.getName());
        assertEquals(1, operation.getInputTypes().size());

        DataType input = (DataType) operation.getInputTypes().get(0);
        assertTrue(input instanceof XSDSimpleType);
        assertEquals("string", input.getXsdType().getLocalPart());

        assertEquals(0, operation.getFaultTypes().size());

        DataType output = (DataType) operation.getOutputType();
        assertTrue(output instanceof XSDSimpleType);
        assertEquals("float", output.getXsdType().getLocalPart());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(factory.newPopulatedExtensionRegistry());
        definition = reader.readWSDL(getClass().getResource("example_1_1.wsdl").toURI().toString());
        portType = definition.getPortType(STOCK_QUOTE_PORT_TYPE);

        schemaCollection = parseSchema(definition);
        processor = new Wsdl11ContractProcessor();
    }

    private XmlSchemaCollection parseSchema(Definition definition) {
        XmlSchemaCollection collection = new XmlSchemaCollection();
        Types types = definition.getTypes();
        for (Object obj : types.getExtensibilityElements()) {
            if (obj instanceof Schema) {
                Schema schema = (Schema) obj;
                Element element = schema.getElement();
                collection.setBaseUri(schema.getDocumentBaseURI());
                collection.read(element);
            }

        }
        return collection;
    }

}
