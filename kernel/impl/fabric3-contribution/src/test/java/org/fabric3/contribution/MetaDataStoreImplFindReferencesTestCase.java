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
package org.fabric3.contribution;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameContributionWire;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.ReferenceIntrospector;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 *
 */
public class MetaDataStoreImplFindReferencesTestCase extends TestCase {
    private MetaDataStoreImpl store;
    private URI contributionUri;
    private URI otherContributionUri;
    private Contribution contribution;
    private QName compositeName;
    private QName otherCompositeName;

    private Contribution otherContribution;

    @SuppressWarnings({"unchecked"})
    public void testFindReferences() throws Exception {
        store.store(contribution);
        store.store(otherContribution);
        QNameSymbol symbol = new QNameSymbol(compositeName);

        ReferenceIntrospector introspector = EasyMock.createMock(ReferenceIntrospector.class);
        EasyMock.expect(introspector.references(EasyMock.isA(ResourceElement.class), EasyMock.isA(ResourceElement.class))).andReturn(true);
        EasyMock.expect(introspector.references(EasyMock.isA(ResourceElement.class), EasyMock.isA(ResourceElement.class))).andReturn(true);
        EasyMock.replay(introspector);
        Map map = Collections.singletonMap(Composite.class.getName(), introspector);
        store.setReferenceIntrospectors(map);
        Set<ResourceElement<QNameSymbol, ?>> set = store.findReferences(contributionUri, symbol);
        assertEquals(2, set.size());
        for (ResourceElement<QNameSymbol, ?> element : set) {
            Composite value = (Composite) element.getValue();
            assertTrue(value.getName().equals(compositeName) || value.getName().equals(otherCompositeName));
        }

    }

    protected void setUp() throws Exception {
        super.setUp();
        ProcessorRegistry processorRegistry = EasyMock.createMock(ProcessorRegistry.class);

        store = new MetaDataStoreImpl(processorRegistry);

        createContributions();

        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators =
                new HashMap<>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);

    }

    private void createContributions() throws MalformedURLException {

        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, 1, "application/xml", false);
        URI profileUri = URI.create("profile");
        contribution.addProfile(profileUri);
        compositeName = new QName("test", "composite");
        createResourceWithComposite(compositeName, contribution);

        otherContributionUri = URI.create("otherContribution");
        URL otherLocationUrl = new URL("file://test");
        otherContribution = new Contribution(otherContributionUri, null, otherLocationUrl, 1, "application/xml", false);
        otherCompositeName = new QName("test", "otherComposite");
        createResourceWithComposite(otherCompositeName, otherContribution);
        wireContributions();
    }

    private void createResourceWithComposite(QName name, Contribution contribution) {
        Resource resource = new Resource(this.contribution, null, "application/xml");
        QNameSymbol symbol = new QNameSymbol(name);
        Composite composite = new Composite(name);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
    }

    private void wireContributions() {
        QNameImport imprt = new QNameImport("test", null);
        contribution.getManifest().addImport(imprt);
        QNameExport export = new QNameExport("test");
        otherContribution.getManifest().addExport(export);

        QNameContributionWire wire = new QNameContributionWire(imprt, export, otherContributionUri, otherContributionUri);
        contribution.addWire(wire);
    }

}
