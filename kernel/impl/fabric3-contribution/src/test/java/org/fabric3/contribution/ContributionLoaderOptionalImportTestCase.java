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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.contribution.generator.ClassLoaderWireGenerator;
import org.fabric3.contribution.generator.JavaContributionWireGenerator;
import org.fabric3.contribution.generator.LocationContributionWireGenerator;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.contribution.wire.ClassLoaderWireBuilder;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * This is more intended to be a integration test then a unit test. *
 */
public class ContributionLoaderOptionalImportTestCase extends TestCase {
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore store;
    private ClasspathProcessorRegistry processorRegistry;
    private Map<Class<?>, ClassLoaderWireGenerator<?>> generators;
    private ClassLoaderWireBuilder builder;
    private HostInfo info;

    private Contribution contribution;
    private URI contributionUri;

    private MultiParentClassLoader hostClassLoader;

    public void testOptionalImportLoad() throws Exception {
        ContributionLoaderMonitor monitor = EasyMock.createNiceMock(ContributionLoaderMonitor.class);
        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder, monitor);
        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info, monitor);

        MultiParentClassLoader classLoader = (MultiParentClassLoader) loader.load(contribution);
        assertTrue(classLoader.getParents().contains(hostClassLoader));

        EasyMock.verify(classLoaderRegistry, store, processorRegistry, builder);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createContribution();

        hostClassLoader = new MultiParentClassLoader(Names.HOST_CONTRIBUTION, getClass().getClassLoader());

        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION)).andReturn(hostClassLoader);
        classLoaderRegistry.register(EasyMock.eq(contributionUri), EasyMock.isA(MultiParentClassLoader.class));

        setupStore();

        setupGenerators();

        processorRegistry = EasyMock.createMock(ClasspathProcessorRegistry.class);
        List<URL> classpath = Collections.emptyList();
        EasyMock.expect(processorRegistry.process(contribution)).andReturn(classpath);

        builder = EasyMock.createMock(ClassLoaderWireBuilder.class);

        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getTempDir()).andReturn(new File(""));

        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        EasyMock.replay(info);

    }

    private void setupGenerators() {
        ClassLoaderWireGenerator javaGenerator = new JavaContributionWireGenerator();
        ClassLoaderWireGenerator locationGenerator = new LocationContributionWireGenerator();
        generators = new HashMap<>();
        generators.put(JavaContributionWire.class, javaGenerator);
        generators.put(LocationContributionWire.class, locationGenerator);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void setupStore() {
        store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.resolveContributionWires(EasyMock.eq(contributionUri), EasyMock.isA(JavaImport.class))).andThrow(new Fabric3Exception("test"));

        List<ContributionWire<?, ?>> list = Collections.emptyList();
        EasyMock.expect(store.resolveContributionWires(EasyMock.isA(URI.class), EasyMock.isA(ContributionImport.class))).andReturn(list);

    }

    private void createContribution() throws MalformedURLException {
        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, -1, null);
        ContributionManifest manifest = contribution.getManifest();

        // setup imports
        PackageInfo info = new PackageInfo("org.fabric3", false);
        JavaImport imprt = new JavaImport(info);
        manifest.addImport(imprt);

    }
}
