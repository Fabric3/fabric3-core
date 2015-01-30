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
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.contribution.UnresolvedImportException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.contribution.generator.JavaContributionWireGenerator;
import org.fabric3.contribution.generator.LocationContributionWireGenerator;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.contribution.ClassLoaderWireGenerator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 * This is more intended to be a integration test then a unit test.
 */
public class ContributionLoaderImplTestCase extends TestCase {
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore store;
    private ClasspathProcessorRegistry processorRegistry;
    private Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators;
    private ClassLoaderWireBuilder builder;
    private HostInfo info;

    private Contribution contribution;
    private URI contributionUri;

    private MultiParentClassLoader hostClassLoader;
    private MultiParentClassLoader extensionClassLoader;

    private JavaImport imprt;
    private JavaExport export;
    private URI extensionProviderUri;
    private URL locationUrl;

    public void testLoad() throws Exception {
        ContributionLoaderMonitor monitor = EasyMock.createNiceMock(ContributionLoaderMonitor.class);
        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder, monitor);

        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info, monitor);

        MultiParentClassLoader classLoader = (MultiParentClassLoader) loader.load(contribution);
        assertTrue(classLoader.getParents().contains(hostClassLoader));

        // verify classloader has the extension provider as an extension and the provider has the classloader as an extension
        Field extensions = MultiParentClassLoader.class.getDeclaredField("extensions");
        extensions.setAccessible(true);
        assertTrue(((List) extensions.get(classLoader)).contains(extensionClassLoader));
        assertTrue(((List) extensions.get(extensionClassLoader)).contains(classLoader));

        // verify location url
        assertTrue(Arrays.asList(classLoader.getURLs()).contains(locationUrl));

        EasyMock.verify(classLoaderRegistry, store, processorRegistry, builder);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createContribution();

        hostClassLoader = new MultiParentClassLoader(Names.HOST_CONTRIBUTION, getClass().getClassLoader());
        extensionClassLoader = new MultiParentClassLoader(extensionProviderUri, getClass().getClassLoader());

        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION)).andReturn(hostClassLoader);
        EasyMock.expect(classLoaderRegistry.getClassLoader(extensionProviderUri)).andReturn(extensionClassLoader).times(2);
        classLoaderRegistry.register(EasyMock.eq(contributionUri), EasyMock.isA(MultiParentClassLoader.class));

        setupStore();

        setupGenerators();

        processorRegistry = EasyMock.createMock(ClasspathProcessorRegistry.class);
        List<URL> classpath = Collections.singletonList(locationUrl);
        EasyMock.expect(processorRegistry.process(locationUrl, Collections.<Library>emptyList())).andReturn(classpath);

        builder = EasyMock.createMock(ClassLoaderWireBuilder.class);
        builder.build(EasyMock.isA(MultiParentClassLoader.class), EasyMock.isA(PhysicalClassLoaderWireDefinition.class));
        EasyMock.expectLastCall().times(2);

        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        EasyMock.expect(info.getTempDir()).andReturn(new File(""));
        EasyMock.replay(info);

    }

    private void setupGenerators() {
        ClassLoaderWireGenerator javaGenerator = new JavaContributionWireGenerator();
        ClassLoaderWireGenerator locationGenerator = new LocationContributionWireGenerator();
        generators = new HashMap<>();
        generators.put(JavaContributionWire.class, javaGenerator);
        generators.put(LocationContributionWire.class, locationGenerator);
    }

    private void setupStore() throws UnresolvedImportException {
        URI importUri = URI.create("import");
        URI exportUri = URI.create("export");
        JavaContributionWire javaWire = new JavaContributionWire(imprt, export, importUri, exportUri);
        List<ContributionWire<?, ?>> javaWires = Collections.<ContributionWire<?, ?>>singletonList(javaWire);

        LocationContributionWire hostWire = new LocationContributionWire(null, null, importUri, exportUri);
        List<ContributionWire<?, ?>> hostWires = Collections.<ContributionWire<?, ?>>singletonList(hostWire);

        store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.resolveContributionWires(EasyMock.eq(contributionUri), EasyMock.isA(JavaImport.class))).andReturn(javaWires);
        EasyMock.expect(store.resolveContributionWires(EasyMock.eq(contributionUri), EasyMock.isA(ContributionImport.class))).andReturn(hostWires);

        Contribution extensionProvider = new Contribution(extensionProviderUri);
        EasyMock.expect(store.resolveExtensionProviders("extensionPoint")).andReturn(Collections.singletonList(extensionProvider));

        EasyMock.expect(store.resolveExtensionPoints("extend")).andReturn(Collections.singletonList(extensionProvider));

    }

    private void createContribution() throws MalformedURLException {
        contributionUri = URI.create("contribution");
        locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, -1, null, false);
        ContributionManifest manifest = contribution.getManifest();

        // setup imports
        PackageInfo info = new PackageInfo("org.fabric3");
        imprt = new JavaImport(info);
        export = new JavaExport(info);
        manifest.addImport(imprt);

        // setup extension points
        manifest.addExtensionPoint("extensionPoint");

        manifest.addExtend("extend");

        extensionProviderUri = URI.create("extensionProvider");
    }
}
