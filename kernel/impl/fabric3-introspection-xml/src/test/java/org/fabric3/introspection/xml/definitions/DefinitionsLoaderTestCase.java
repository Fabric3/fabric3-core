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
package org.fabric3.introspection.xml.definitions;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.host.Namespaces;
import org.fabric3.host.stream.Source;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicyPhase;
import org.fabric3.model.type.definitions.PolicySet;
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
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class DefinitionsLoaderTestCase extends TestCase {

    private static final Object WS_POLICY_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy";

    private static final QName BINDING_QNAME = new QName("http://docs.oasis-open.org/ns/opencsa/sca/200912", "binding");
    private static final QName INTERCEPTED_INTENT = new QName(Namespaces.F3, "intercepted");
    private static final QName QUALIFIER_INTENT = new QName(Namespaces.F3, "qualifier");
    private static final QName QUALIFIER_QUALFIED1_INTENT = new QName(Namespaces.F3, "qualifier.qualifier1");
    private static final QName QUALIFIER_QUALFIED2_INTENT = new QName(Namespaces.F3, "qualifier.qualifier2");
    private static final QName PROVIDED_INTENT = new QName(Namespaces.F3, "provided");
    private static final QName PROVIDED_POLICY = new QName(Namespaces.F3, "providedPolicy");
    private static final QName INTERCEPTED_POLICY = new QName(Namespaces.F3, "interceptedPolicy");
    private static final QName WS_POLICY = new QName(Namespaces.F3, "wsPolicy");

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
                assertEquals(Namespaces.F3, expressionName.getNamespaceURI());
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
                assertEquals(Namespaces.F3, expressionName.getNamespaceURI());
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

        private Map<QName, TypeLoader<?>> loaders = new HashMap<QName, TypeLoader<?>>();

        public void registerLoader(QName element, TypeLoader<?> loader) throws IllegalStateException {
            loaders.put(element, loader);
        }

        public void unregisterLoader(QName element) {
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
