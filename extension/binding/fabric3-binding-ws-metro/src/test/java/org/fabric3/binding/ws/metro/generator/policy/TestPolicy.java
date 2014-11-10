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
public interface TestPolicy {

    String POLICY_ID = "DoubleItBindingPolicy";

    String POLICY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<Policy wsu:Id=\"DoubleItBindingPolicy\"\n" +
            "        xmlns:tns=\"urn:helloworld\"\n" +
            "        xmlns=\"http://www.w3.org/ns/ws-policy\"\n" +
            "        xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "        xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
            "        xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"\n" +
            "        xmlns:fi=\"http://java.sun.com/xml/ns/wsit/2006/09/policy/fastinfoset/service\"\n" +
            "        xmlns:tcp=\"http://java.sun.com/xml/ns/wsit/2006/09/policy/soaptcp/service\"\n" +
            "        xmlns:wsp=\"http://www.w3.org/ns/ws-policy\"\n" +
            "        xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\"\n" +
            "        xmlns:sc=\"http://schemas.sun.com/2006/03/wss/server\"\n" +
            "        xmlns:wspp=\"http://java.sun.com/xml/ns/wsit/policy\"\n" +
            "        xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\"\n" +
            "        xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\">\n" +
            "    <wsp:ExactlyOne>\n" +
            "        <wsp:All>\n" +
            "            <wsam:Addressing wsp:Optional=\"false\"/>\n" +
            "            <sp:TransportBinding>\n" +
            "                <wsp:Policy>\n" +
            "                    <sp:TransportToken>\n" +
            "                        <wsp:Policy>\n" +
            "                            <sp:HttpsToken RequireClientCertificate=\"false\"/>\n" +
            "                        </wsp:Policy>\n" +
            "                    </sp:TransportToken>\n" +
            "                    <sp:Layout>\n" +
            "                        <wsp:Policy>\n" +
            "                            <sp:Lax/>\n" +
            "                        </wsp:Policy>\n" +
            "                    </sp:Layout>\n" +
            "                    <sp:IncludeTimestamp/>\n" +
            "                    <sp:AlgorithmSuite>\n" +
            "                        <wsp:Policy>\n" +
            "                            <sp:Basic128/>\n" +
            "                        </wsp:Policy>\n" +
            "                    </sp:AlgorithmSuite>\n" +
            "                </wsp:Policy>\n" +
            "            </sp:TransportBinding>\n" +
            "            <sp:Wss10/>\n" +
            "            <sp:SignedSupportingTokens>\n" +
            "                <wsp:Policy>\n" +
            "                    <sp:UsernameToken sp:IncludeToken=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/AlwaysToRecipient\">\n" +
            "                        <wsp:Policy>\n" +
            "                            <sp:WssUsernameToken10/>\n" +
            "                        </wsp:Policy>\n" +
            "                    </sp:UsernameToken>\n" +
            "                </wsp:Policy>\n" +
            "            </sp:SignedSupportingTokens>\n" +
            "            <sc:ValidatorConfiguration wspp:visibility=\"private\">\n" +
            "                <sc:Validator name=\"usernameValidator\"\n" +
            "                              classname=\"com.mycompany.webservice.service.PlainTextPasswordValidator\"/>\n" +
            "            </sc:ValidatorConfiguration>\n" +
            "        </wsp:All>\n" +
            "    </wsp:ExactlyOne>\n" +
            "</Policy>\n";
}
