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
*/
package org.fabric3.introspection.xml.definitions;

import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.model.type.definitions.IntentMap;
import org.fabric3.model.type.definitions.IntentQualifier;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * @version $Rev$ $Date$
 */
public class PolicySetLoaderTestCase extends TestCase {
    private static String VALID_WS_POLICY_SET =
            "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
                    " xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
                    " xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'" +
                    " xmlns:wsp='http://www.w3.org/ns/ws-policy'" +
                    " xmlns:wsam='http://www.w3.org/2007/05/addressing/metadata'" +
                    " xmlns:sp='http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702'" +
                    " xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
                    "<policySet name='TransportConfidentialityPolicy' provides='sca:confidentiality.transport' appliesTo='sca:binding.ws'>" +
                    "        <wsp:Policy wsu:Id='TransportConfidentialityPolicy'>" +
                    "            <sp:TransportBinding>" +
                    "                <wsp:Policy>" +
                    "                    <sp:TransportToken>" +
                    "                        <wsp:Policy>" +
                    "                            <sp:HttpsToken>" +
                    "                                <wsp:Policy/>" +
                    "                            </sp:HttpsToken>" +
                    "                        </wsp:Policy>" +
                    "                    </sp:TransportToken>" +
                    "                    <sp:AlgorithmSuite>" +
                    "                        <wsp:Policy>" +
                    "                            <sp:Basic256Rsa15/>" +
                    "                        </wsp:Policy>" +
                    "                    </sp:AlgorithmSuite>" +
                    "                    <sp:Layout>" +
                    "                        <wsp:Policy>" +
                    "                            <sp:Lax/>" +
                    "                        </wsp:Policy>" +
                    "                    </sp:Layout>" +
                    "                    <sp:IncludeTimestamp/>" +
                    "                </wsp:Policy>" +
                    "            </sp:TransportBinding>" +
                    "        </wsp:Policy>" +
                    "</policySet>" +
                    "</definitions";


    private static String VALID_INTENT_MAP = "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            " xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
            "<policySet name='SecureMessagingPolicies'" +
            "           provides='confidentiality'" +
            "           appliesTo='//binding.ws'" +
            "           xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            "           xmlns:wsp='http://schemas.xmlsoap.org/ws/2004/09/policy'>" +
            "    <intentMap provides='confidentiality'>" +
            "        <qualifier name='transport'>" +
            "            <wsp:PolicyAttachment>" +
            "            </wsp:PolicyAttachment>" +
            "        </qualifier>" +
            "        <qualifier name='message'>" +
            "            <wsp:PolicyAttachment>" +
            "            </wsp:PolicyAttachment>" +
            "        </qualifier>" +
            "    </intentMap>" +
            "</policySet>" +
            "</definitions>";

    private PolicySetLoader loader;
    private XMLInputFactory factory;
    private IntrospectionContext context;

    public void testWSPolicyParse() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(VALID_WS_POLICY_SET.getBytes()));
        reader.nextTag();
        reader.nextTag();
        PolicySet policySet = loader.load(reader, context);
        assertEquals("sca:binding.ws", policySet.getAppliesTo());
        assertEquals("TransportConfidentialityPolicy", policySet.getName().getLocalPart());
        assertNotNull(policySet.getExpression());
        assertTrue(context.getErrors().isEmpty());
    }

    public void testIntentMapParse() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(VALID_INTENT_MAP.getBytes()));
        reader.nextTag();
        reader.nextTag();
        PolicySet policySet = loader.load(reader, context);
        assertNull(policySet.getExpression());
        assertEquals(1, policySet.getIntentMaps().size());
        for (IntentMap intentMap : policySet.getIntentMaps()) {
            QName provides = intentMap.getProvides();
            assertEquals("confidentiality", provides.getLocalPart());
            assertEquals(2, intentMap.getQualifiers().size());
            for (IntentQualifier qualifier : intentMap.getQualifiers()) {
                assertTrue("transport".equals(qualifier.getName()) || "message".equals(qualifier.getName()));
                assertNotNull(qualifier.getContent());
            }
        }
        assertTrue(context.getErrors().isEmpty());
    }


    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        loader = new PolicySetLoader(loaderHelper);
    }


}
