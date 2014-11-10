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
package org.fabric3.fabric.domain.generator.policy;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.api.model.type.definitions.BindingType;
import org.fabric3.api.model.type.definitions.ImplementationType;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.IntentMap;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.api.model.type.definitions.Qualifier;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.domain.generator.policy.PolicyActivationException;

import static org.fabric3.api.model.type.definitions.IntentType.INTERACTION;

/**
 *
 */
public class DefaultPolicyRegistryTestCase extends TestCase {
    private DefaultPolicyRegistry registry;
    private MetaDataStore store;

    public void testActivateDeactivatePolicySet() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QName name = new QName("test", "policyset");
        QNameSymbol symbol = new QNameSymbol(name);
        PolicySet policySet = new PolicySet(name, null, null, null, null, null, Collections.<IntentMap>emptySet(), null);
        ResourceElement<QNameSymbol, PolicySet> element = new ResourceElement<>(symbol, policySet);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        registry.activateDefinitions(uri);
        assertNotNull(registry.getDefinition(name, PolicySet.class));

        registry.deactivateDefinitions(uri);
        assertNull(registry.getDefinition(name, PolicySet.class));
    }

    public void testActivateDeactivateIntent() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QName name = new QName("test", "intent");
        QNameSymbol symbol = new QNameSymbol(name);
        Intent intent = new Intent(name,
                                   null,
                                   Collections.<QName>emptySet(),
                                   Collections.<Qualifier>emptySet(),
                                   false,
                                   Collections.<QName>emptySet(),
                                   INTERACTION,
                                   false);
        ResourceElement<QNameSymbol, Intent> element = new ResourceElement<>(symbol, intent);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        registry.activateDefinitions(uri);
        assertNotNull(registry.getDefinition(name, Intent.class));

        registry.deactivateDefinitions(uri);
        assertNull(registry.getDefinition(name, Intent.class));
    }

    public void testRequiredIntentsNotFound() throws Exception {
        QName name = new QName("test", "intent");
        QName required = new QName("test", "doesnotexist");
        Intent intent = new Intent(name,
                                   null,
                                   Collections.singleton(required),
                                   Collections.<Qualifier>emptySet(),
                                   false,
                                   Collections.<QName>emptySet(),
                                   INTERACTION,
                                   false);

        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Intent> element = new ResourceElement<>(symbol, intent);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);


        try {
            registry.activateDefinitions(uri);
            fail("Non-existent required intent should raise an exception");
        } catch (PolicyActivationException e) {
            // expected
        }
    }

    public void testExcludedIntentsNotFound() throws Exception {
        QName name = new QName("test", "intent");
        QName excluded = new QName("test", "doesnotexist");
        Intent intent = new Intent(name,
                                   null,
                                   Collections.<QName>emptySet(),
                                   Collections.<Qualifier>emptySet(),
                                   false,
                                   Collections.<QName>singleton(excluded),
                                   INTERACTION,
                                   false);

        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Intent> element = new ResourceElement<>(symbol, intent);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);


        try {
            registry.activateDefinitions(uri);
            fail("Non-existent excluded intent should raise an exception");
        } catch (PolicyActivationException e) {
            // expected
        }
    }


    public void testIntentMapDoesNotSpecifyQualifier() throws Exception {
        QName intentName = new QName("test", "intent");
        Qualifier qualifier = new Qualifier("fooQualifier", true);
        Intent intent = new Intent(intentName,
                                   null,
                                   Collections.<QName>emptySet(),
                                   Collections.<Qualifier>singleton(qualifier),
                                   false,
                                   Collections.<QName>emptySet(),
                                   INTERACTION,
                                   false);

        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QNameSymbol symbol = new QNameSymbol(intentName);
        ResourceElement<QNameSymbol, Intent> intentElement = new ResourceElement<>(symbol, intent);
        resource.addResourceElement(intentElement);
        contribution.addResource(resource);

        IntentMap intentMap = new IntentMap(intentName);
        QName policySetName = new QName("test", "policySet");
        PolicySet policySet = new PolicySet(policySetName, null, null, null, null, null, Collections.singleton(intentMap), null);
        ResourceElement<QNameSymbol, PolicySet> policyElement = new ResourceElement<>(symbol, policySet);
        Resource policyResource = new Resource(contribution, source, "text/xml");
        policyResource.addResourceElement(policyElement);
        contribution.addResource(policyResource);


        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);


        try {
            registry.activateDefinitions(uri);
            fail("IntentMap does not specify the intent qualifier and should raise an exception");
        } catch (PolicyActivationException e) {
            // expected
        }
    }


    public void testActivateDeactivateBindingType() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QName name = new QName("test", "bindingtype");
        QNameSymbol symbol = new QNameSymbol(name);
        BindingType bindingType = new BindingType(name, null, null);
        ResourceElement<QNameSymbol, BindingType> element = new ResourceElement<>(symbol, bindingType);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        registry.activateDefinitions(uri);
        assertNotNull(registry.getDefinition(name, BindingType.class));

        registry.deactivateDefinitions(uri);
        assertNull(registry.getDefinition(name, BindingType.class));
    }

    public void testActivateDeactivateImplementationType() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        QName name = new QName("test", "impltype");
        QNameSymbol symbol = new QNameSymbol(name);
        ImplementationType implementationType = new ImplementationType(name, null, null);
        ResourceElement<QNameSymbol, ImplementationType> element = new ResourceElement<>(symbol, implementationType);
        resource.addResourceElement(element);
        contribution.addResource(resource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);

        registry.activateDefinitions(uri);
        assertNotNull(registry.getDefinition(name, ImplementationType.class));

        registry.deactivateDefinitions(uri);
        assertNull(registry.getDefinition(name, ImplementationType.class));
    }


    public void testPolicySetReferenceProvidesAnIntentNotProvidedByParentPolicySet() throws Exception {

        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        URL url = new URL("file://test");
        Source source = new UrlSource(url);
        Resource resource = new Resource(contribution, source, "text/xml");
        contribution.addResource(resource);

        QName policySetName = new QName("test", "policySet");
        QName refPolicySetName = new QName("test", "referencedPolicySet");
        Set<QName> provides = Collections.singleton(new QName("foo", "bar"));
        PolicySet policySet = new PolicySet(policySetName, provides, null, null, null, null, Collections.<IntentMap>emptySet(), null);

        Set<QName> refProvides = Collections.singleton(new QName("foo", "baz"));
        PolicySet referencedPolicySet = new PolicySet(refPolicySetName, refProvides, null, null, null, null, Collections.<IntentMap>emptySet(), null);
        policySet.setPolicySetReferences(Collections.singleton(refPolicySetName));

        QNameSymbol symbol = new QNameSymbol(policySetName);
        ResourceElement<QNameSymbol, PolicySet> policyElement = new ResourceElement<>(symbol, policySet);
        Resource policyResource = new Resource(contribution, source, "text/xml");
        policyResource.addResourceElement(policyElement);
        contribution.addResource(policyResource);


        QNameSymbol refSymbol = new QNameSymbol(refPolicySetName);
        ResourceElement<QNameSymbol, PolicySet> refPolicyElement = new ResourceElement<>(refSymbol, referencedPolicySet);
        Resource refPolicyResource = new Resource(contribution, source, "text/xml");
        refPolicyResource.addResourceElement(refPolicyElement);
        contribution.addResource(refPolicyResource);

        EasyMock.expect(store.find(uri)).andReturn(contribution).atLeastOnce();
        EasyMock.replay(store);


        try {
            registry.activateDefinitions(uri);
            fail("Exception should have been raised as PolicyReference provides an intent not provided by the parent");
        } catch (PolicyActivationException e) {
            // expected
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        store = EasyMock.createMock(MetaDataStore.class);
        registry = new DefaultPolicyRegistry(store);
    }
}
