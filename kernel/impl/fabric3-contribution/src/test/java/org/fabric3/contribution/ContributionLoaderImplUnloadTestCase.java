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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.contribution.generator.ClassLoaderWireGenerator;
import org.fabric3.contribution.wire.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * This is more intended to be a integration test then a unit test. *
 */
public class ContributionLoaderImplUnloadTestCase extends TestCase {
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore store;
    private ClasspathProcessorRegistry processorRegistry;
    private Map<Class<?>, ClassLoaderWireGenerator<?>> generators;
    private ClassLoaderWireBuilder builder;
    private HostInfo info;

    private Contribution contribution;
    private URI contributionUri;

    private Contribution dependentContribution;

    public void testUnLoad() throws Exception {
        MultiParentClassLoader contributionClassLoader = new MultiParentClassLoader(contributionUri, getClass().getClassLoader());
        EasyMock.expect(classLoaderRegistry.unregister(EasyMock.eq(contributionUri))).andReturn(contributionClassLoader);

        ContributionLoaderMonitor monitor = EasyMock.createNiceMock(ContributionLoaderMonitor.class);
        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder, monitor);

        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info, monitor);

        loader.unload(contribution);

        EasyMock.verify(classLoaderRegistry, store, processorRegistry, builder);
    }

    public void testErrorDependentUnLoad() throws Exception {
        dependentContribution.install();

        ContributionLoaderMonitor monitor = EasyMock.createNiceMock(ContributionLoaderMonitor.class);
        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder, monitor);
        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info, monitor);

        try {
            loader.unload(contribution);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
        EasyMock.verify(classLoaderRegistry, store, processorRegistry, builder);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createContributions();

        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);

        setupStore();

        generators = new HashMap<>();

        processorRegistry = EasyMock.createMock(ClasspathProcessorRegistry.class);

        builder = EasyMock.createMock(ClassLoaderWireBuilder.class);

        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        EasyMock.expect(info.getTempDir()).andReturn(new File(""));
        EasyMock.replay(info);

    }

    private void setupStore() {
        store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.resolveDependentContributions(contributionUri)).andReturn(Collections.singleton(dependentContribution));
    }

    private void createContributions() {
        contributionUri = URI.create("contribution");
        contribution = new Contribution(contributionUri);

        URI dependentUri = URI.create("dependentUri");
        dependentContribution = new Contribution(dependentUri);
    }
}
