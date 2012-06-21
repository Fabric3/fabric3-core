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
package org.fabric3.binding.ws.metro.generator.policy;

/**
 * @version $Rev$ $Date$
 */
public interface TestWsdl {


    String WSDL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<definitions name=\"HelloWorld\"\n" +
            "             targetNamespace=\"urn:helloworld\"\n" +
            "             xmlns:tns=\"urn:helloworld\"\n" +
            "             xmlns=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
            "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "             xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\">\n" +
            "\n" +
            "    <types>\n" +
            "        <xsd:schema xmlns=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" targetNamespace=\"urn:helloworld\">\n" +
            "            <complexType name=\"HelloFault\">\n" +
            "                <sequence>\n" +
            "                    <element name=\"code\" type=\"xsd:string\"/>\n" +
            "                    <element name=\"detail\" type=\"xsd:string\"/>\n" +
            "                </sequence>\n" +
            "            </complexType>\n" +
            "            <element name=\"request\" type=\"xsd:string\"/>\n" +
            "            <element name=\"helloFault\" type=\"tns:HelloFault\"/>\n" +
            "            <element name=\"response\" type=\"xsd:string\"/>\n" +
            "        </xsd:schema>\n" +
            "    </types>\n" +
            "\n" +
            "    <message name=\"sayHelloRequest\">\n" +
            "        <part name=\"name\" element=\"tns:request\"/>\n" +
            "    </message>\n" +
            "\n" +
            "    <message name=\"sayHelloResponse\">\n" +
            "        <part name=\"result\" element=\"tns:response\"/>\n" +
            "    </message>\n" +
            "\n" +
            "    <message name=\"helloFault\">\n" +
            "        <part name=\"helloFault\" element=\"tns:helloFault\"/>\n" +
            "    </message>\n" +
            "\n" +
            "    <portType name=\"HelloWorldPortType\">\n" +
            "        <operation name=\"sayHello\">\n" +
            "            <input message=\"tns:sayHelloRequest\"/>\n" +
            "            <output message=\"tns:sayHelloResponse\"/>\n" +
            "            <fault name=\"helloFault\" message=\"tns:helloFault\"/>\n" +
            "        </operation>\n" +
            "    </portType>\n" +
            "\n" +
            "    <binding name=\"HelloWorldBinding\" type=\"tns:HelloWorldPortType\">\n" +
            "        <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
            "        <operation name=\"sayHello\">\n" +
            "            <soap:operation soapAction=\"\"/>\n" +
            "            <input>\n" +
            "                <soap:body use=\"literal\"/>\n" +
            "            </input>\n" +
            "            <output>\n" +
            "                <soap:body use=\"literal\"/>\n" +
            "            </output>\n" +
            "            <fault name=\"helloFault\">\n" +
            "                <soap:fault name=\"helloFault\" use=\"literal\"/>\n" +
            "            </fault>\n" +
            "        </operation>\n" +
            "    </binding>\n" +
            "\n" +
            "    <service name=\"HelloWorldService\">\n" +
            "        <port name=\"HelloWorldPort\" binding=\"tns:HelloWorldBinding\">\n" +
            "            <soap:address location=\"REPLACE_WITH_ACTUAL_URL\"/>\n" +
            "        </port>\n" +
            "    </service>\n" +
            "\n" +
            "</definitions>";
}