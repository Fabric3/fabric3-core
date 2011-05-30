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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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

import org.fabric3.contribution.generator.JavaContributionWireGenerator;
import org.fabric3.contribution.generator.LocationContributionWireGenerator;
import org.fabric3.contribution.manifest.ContributionImport;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.UnresolvedImportException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Library;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;
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
        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder);
        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info);

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
        List<URL>  classpath = Collections.singletonList(locationUrl);
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
        generators = new HashMap<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>>();
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
