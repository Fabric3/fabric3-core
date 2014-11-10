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
package org.fabric3.binding.ws.metro.generator.policy;

/**
 *
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