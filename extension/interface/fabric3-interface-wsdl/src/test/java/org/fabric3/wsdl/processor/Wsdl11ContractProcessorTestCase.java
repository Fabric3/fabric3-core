/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class Wsdl11ContractProcessorTestCase extends TestCase {
    private static final QName STOCK_QUOTE_PORT_TYPE = new QName("http://example.com/stockquote.wsdl", "StockQuotePortType");
    private static final QName STOCK_QUOTE_WSDL = new QName("http://example.com/stockquote.wsdl", "Wsdl");
    private WsdlContractProcessor processor;
    private PortType portType;
    private XmlSchemaCollection schemaCollection;

    @SuppressWarnings({"unchecked"})
    public void testIntrospect() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        WsdlServiceContract contract = processor.introspect(portType, STOCK_QUOTE_WSDL, schemaCollection, context);

        assertEquals(1, contract.getOperations().size());
        Operation operation = contract.getOperations().get(0);
        assertEquals("GetLastTradePrice", operation.getName());
        assertEquals(1, operation.getInputTypes().size());

        DataType<QName> input = (DataType<QName>) operation.getInputTypes().get(0);
        assertTrue(input instanceof XSDSimpleType);
        assertEquals("string", input.getLogical().getLocalPart());

        assertEquals(0, operation.getFaultTypes().size());

        DataType<QName> output = (DataType<QName>) operation.getOutputType();
        assertTrue(output instanceof XSDSimpleType);
        assertEquals("float", output.getLogical().getLocalPart());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setExtensionRegistry(factory.newPopulatedExtensionRegistry());
        Definition definition = reader.readWSDL(getClass().getResource("example_1_1.wsdl").toURI().toString());
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
