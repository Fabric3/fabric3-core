/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
