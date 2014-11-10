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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.definitions;

import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.api.model.type.definitions.IntentMap;
import org.fabric3.api.model.type.definitions.IntentQualifier;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 *
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

    private static String INVALID_PROVIDES_INTENT_MAP = "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            " xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
            "<policySet name='SecureMessagingPolicies'" +
            "           provides='confidentiality'" +
            "           appliesTo='//binding.ws'" +
            "           xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            "           xmlns:wsp='http://schemas.xmlsoap.org/ws/2004/09/policy'>" +
            "    <intentMap provides='invalidname'>" +
            "        <qualifier name='transport'>" +
            "            <wsp:PolicyAttachment>" +
            "            </wsp:PolicyAttachment>" +
            "        </qualifier>" +
            "    </intentMap>" +
            "</policySet>" +
            "</definitions>";

    private static String INVALID_DUPLICATE_PROVIDES_INTENT_MAP = "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            " xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
            "<policySet name='SecureMessagingPolicies'" +
            "           provides='confidentiality'" +
            "           appliesTo='//binding.ws'" +
            "           xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            "           xmlns:wsp='http://schemas.xmlsoap.org/ws/2004/09/policy'>" +
            "    <intentMap provides='invalidname'>" +
            "        <qualifier name='transport'>" +
            "            <wsp:PolicyAttachment>" +
            "            </wsp:PolicyAttachment>" +
            "        </qualifier>" +
            "    </intentMap>" +
            "    <intentMap provides='invalidname'>" +
            "        <qualifier name='transport'>" +
            "            <wsp:PolicyAttachment>" +
            "            </wsp:PolicyAttachment>" +
            "        </qualifier>" +
            "    </intentMap>" +
            "</policySet>" +
            "</definitions>";


    private static String POLICY_SET_REFERENCE = "<definitions xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            " xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'>" +
            "<policySet name='SecureMessagingPolicies'" +
            "           provides='confidentiality'" +
            "           appliesTo='//binding.ws'" +
            "           xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            "           xmlns:wsp='http://schemas.xmlsoap.org/ws/2004/09/policy'>" +
            "    <policySetReference name='sca:ref1'/>" +
            "    <policySetReference name='sca:ref2'/>" +
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

    public void testInvalidProvidesInIntentMap() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_PROVIDES_INTENT_MAP.getBytes()));
        reader.nextTag();
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof InvalidValue);
    }

    public void testInvalidDuplicateProvidesInIntentMaps() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_DUPLICATE_PROVIDES_INTENT_MAP.getBytes()));
        reader.nextTag();
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof DuplicateIntentMap);
    }


    public void testPolicySetReferenceParse() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(POLICY_SET_REFERENCE.getBytes()));
        reader.nextTag();
        reader.nextTag();
        PolicySet policySet = loader.load(reader, context);
        assertEquals(2, policySet.getPolicySetReferences().size());
        for (QName name : policySet.getPolicySetReferences()) {
            assertTrue("ref1".equals(name.getLocalPart()) || "ref2".equals(name.getLocalPart()));
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
