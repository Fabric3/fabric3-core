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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.api.host.stream.Source;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.api.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicyPhase;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 *
 */
public class DefinitionsLoaderTestCase extends TestCase {

    private static final Object WS_POLICY_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    private static final QName BINDING_QNAME = new QName("http://docs.oasis-open.org/ns/opencsa/sca/200912", "binding");
    private static final QName INTERCEPTED_INTENT = new QName(org.fabric3.api.Namespaces.F3, "intercepted");
    private static final QName QUALIFIER_INTENT = new QName(org.fabric3.api.Namespaces.F3, "qualifier");
    private static final QName QUALIFIER_QUALFIED1_INTENT = new QName(org.fabric3.api.Namespaces.F3, "qualifier.qualifier1");
    private static final QName QUALIFIER_QUALFIED2_INTENT = new QName(org.fabric3.api.Namespaces.F3, "qualifier.qualifier2");
    private static final QName PROVIDED_INTENT = new QName(org.fabric3.api.Namespaces.F3, "provided");
    private static final QName PROVIDED_POLICY = new QName(org.fabric3.api.Namespaces.F3, "providedPolicy");
    private static final QName INTERCEPTED_POLICY = new QName(org.fabric3.api.Namespaces.F3, "interceptedPolicy");
    private static final QName WS_POLICY = new QName(org.fabric3.api.Namespaces.F3, "wsPolicy");

    private DefinitionsLoader loader;
    private Resource resource;
    private XMLStreamReader reader;

    @SuppressWarnings({"unchecked"})
    public void testLoad() throws Exception {

        IntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, resource, context);

        List<ResourceElement<?, ?>> elements = resource.getResourceElements();
        assertEquals(7, elements.size());

        verifyIntent(elements);
        verifyQualifierIntent(elements);
        verifyInterceptedPolicy(elements);
        verifyProvidedPolicy(elements);
        verifyWsPolicy(elements);

    }

    @SuppressWarnings({"unchecked"})
    private void verifyIntent(List<ResourceElement<?, ?>> elements) {
        boolean verified = false;
        for (ResourceElement<?, ?> element : elements) {
            Symbol symbol = element.getSymbol();
            if (INTERCEPTED_INTENT.equals(symbol.getKey())) {
                Intent intent = (Intent) element.getValue();
                assertNotNull(intent);
                assertEquals(INTERCEPTED_INTENT, intent.getName());
                assertTrue(intent.doesConstrain(BINDING_QNAME));
                assertFalse(intent.isProfile());
                assertFalse(intent.isQualified());
                assertNull(intent.getQualifiable());
                assertEquals(0, intent.getRequires().size());
                verified = true;

            }
        }
        assertTrue(verified);
    }

    @SuppressWarnings({"unchecked"})
    private void verifyQualifierIntent(List<ResourceElement<?, ?>> elements) {
        // verify intents with qualifier elements are expanded
        int count = 0;
        for (ResourceElement<?, ?> element : elements) {
            Symbol symbol = element.getSymbol();
            Object key = symbol.getKey();
            if (QUALIFIER_INTENT.equals(key)) {
                count++;
            } else if (QUALIFIER_QUALFIED1_INTENT.equals(key)) {
                Intent intent = (Intent) element.getValue();
                assertEquals(1, intent.getExcludes().size());
                assertTrue(intent.getExcludes().contains(QUALIFIER_QUALFIED2_INTENT));
                count++;
            } else if (QUALIFIER_QUALFIED2_INTENT.equals(key)) {
                Intent intent = (Intent) element.getValue();
                assertEquals(1, intent.getExcludes().size());
                assertTrue(intent.getExcludes().contains(QUALIFIER_QUALFIED1_INTENT));
                count++;
            }
        }
        assertEquals(3, count);
    }

    private void verifyInterceptedPolicy(List<ResourceElement<?, ?>> elements) {
        boolean verified = false;
        for (ResourceElement<?, ?> element : elements) {
            Symbol symbol = element.getSymbol();
            if (INTERCEPTED_POLICY.equals(symbol.getKey())) {
                PolicySet policySet = (PolicySet) element.getValue();
                assertNotNull(policySet);
                assertEquals(INTERCEPTED_POLICY, policySet.getName());
                assertTrue(policySet.doesProvide(INTERCEPTED_INTENT));

                QName expressionName = policySet.getExpressionName();
                assertEquals(org.fabric3.api.Namespaces.F3, expressionName.getNamespaceURI());
                assertEquals("interceptor", expressionName.getLocalPart());
                assertEquals(PolicyPhase.INTERCEPTION, policySet.getPhase());
                verified = true;
            }
        }
        assertTrue(verified);
    }

    private void verifyProvidedPolicy(List<ResourceElement<?, ?>> elements) {
        boolean verified = false;
        for (ResourceElement<?, ?> element : elements) {
            Symbol symbol = element.getSymbol();
            if (PROVIDED_POLICY.equals(symbol.getKey())) {
                PolicySet policySet = (PolicySet) element.getValue();
                assertNotNull(policySet);
                assertEquals(PROVIDED_POLICY, policySet.getName());
                assertTrue(policySet.doesProvide(PROVIDED_INTENT));

                QName expressionName = policySet.getExpressionName();
                assertEquals(org.fabric3.api.Namespaces.F3, expressionName.getNamespaceURI());
                assertEquals("someElement", expressionName.getLocalPart());
                assertEquals(PolicyPhase.PROVIDED, policySet.getPhase());
                verified = true;
            }
        }
        assertTrue(verified);
    }

    private void verifyWsPolicy(List<ResourceElement<?, ?>> elements) {
        boolean verified = false;
        for (ResourceElement<?, ?> element : elements) {
            Symbol symbol = element.getSymbol();
            if (WS_POLICY.equals(symbol.getKey())) {
                PolicySet policySet = (PolicySet) element.getValue();
                assertNotNull(policySet);
                assertEquals(WS_POLICY, policySet.getName());

                QName expressionName = policySet.getExpressionName();
                assertEquals(WS_POLICY_NS, expressionName.getNamespaceURI());
                assertEquals("Policy", expressionName.getLocalPart());
                verified = true;
            }
        }
        assertTrue(verified);
    }

    protected void setUp() throws Exception {
        super.setUp();
        // setup loader infrastructure
        LoaderRegistry loaderRegistry = new MockLoaderRegistry();
        loader = new DefinitionsLoader(null, loaderRegistry);
        LoaderHelper helper = new DefaultLoaderHelper();
        IntentLoader intentLoader = new IntentLoader(helper);
        PolicySetLoader policySetLoader = new PolicySetLoader(helper);
        loaderRegistry.registerLoader(DefinitionsLoader.POLICY_SET, policySetLoader);
        loaderRegistry.registerLoader(DefinitionsLoader.INTENT, intentLoader);

        // setup indexed resource
        resource = new Resource(null, null, "application/xml");
        // setup up indexed resource elements
        ResourceElement<QNameSymbol, ?> element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(new QNameSymbol(INTERCEPTED_INTENT));
        resource.addResourceElement(element);
        element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(new QNameSymbol(QUALIFIER_INTENT));
        resource.addResourceElement(element);
        element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(new QNameSymbol(PROVIDED_POLICY));
        resource.addResourceElement(element);
        element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(new QNameSymbol(INTERCEPTED_POLICY));
        resource.addResourceElement(element);
        element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(new QNameSymbol(WS_POLICY));
        resource.addResourceElement(element);

        // setup reader
        InputStream stream = getClass().getResourceAsStream("definitions.xml");
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        while (reader.next() != XMLStreamConstants.START_ELEMENT) {
        }

    }

    @SuppressWarnings("deprecation")
    private static class MockLoaderRegistry implements LoaderRegistry {

        private Map<QName, TypeLoader<?>> loaders = new HashMap<>();

        public void registerLoader(QName element, TypeLoader<?> loader) throws IllegalStateException {
            loaders.put(element, loader);
        }

        public void unregisterLoader(QName element) {
        }

        public boolean isRegistered(QName element) {
            return false;
        }

        @SuppressWarnings("unchecked")
        public <OUTPUT> OUTPUT load(XMLStreamReader reader, Class<OUTPUT> type, IntrospectionContext context) throws XMLStreamException {
            return (OUTPUT) loaders.get(reader.getName()).load(reader, context);
        }

        public <OUTPUT> OUTPUT load(Source source, Class<OUTPUT> type, IntrospectionContext context) throws LoaderException {
            return null;
        }

    }

}
