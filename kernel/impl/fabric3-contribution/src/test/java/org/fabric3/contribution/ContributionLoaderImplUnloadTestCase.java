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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.ContributionInUseException;
import org.fabric3.host.contribution.UnresolvedImportException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;

/**
 * This is more intended to be a integration test then a unit test. *
 */
public class ContributionLoaderImplUnloadTestCase extends TestCase {
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore store;
    private ClasspathProcessorRegistry processorRegistry;
    private Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators;
    private ClassLoaderWireBuilder builder;
    private HostInfo info;

    private Contribution contribution;
    private URI contributionUri;

    private Contribution dependentContribution;
    private URI dependentUri;

    public void testUnLoad() throws Exception {
        MultiParentClassLoader contributionClassLoader = new MultiParentClassLoader(contributionUri, getClass().getClassLoader());
        EasyMock.expect(classLoaderRegistry.unregister(EasyMock.eq(contributionUri))).andReturn(contributionClassLoader);
        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder);

        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info);

        loader.unload(contribution);

        EasyMock.verify(classLoaderRegistry, store, processorRegistry, builder);
    }

    public void testErrorDependentUnLoad() throws Exception {
        dependentContribution.setState(ContributionState.INSTALLED);

        EasyMock.replay(classLoaderRegistry, store, processorRegistry, builder);
        ContributionLoaderImpl loader = new ContributionLoaderImpl(classLoaderRegistry, store, processorRegistry, generators, builder, info);

        try {
            loader.unload(contribution);
            fail();
        } catch (ContributionInUseException e) {
            // expected
            assertTrue(e.getContributions().contains(dependentUri));
        }
        EasyMock.verify(classLoaderRegistry, store, processorRegistry, builder);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createContributions();

        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);

        setupStore();

        generators = new HashMap<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>>();

        processorRegistry = EasyMock.createMock(ClasspathProcessorRegistry.class);

        builder = EasyMock.createMock(ClassLoaderWireBuilder.class);

        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        EasyMock.replay(info);

    }

    private void setupStore() throws UnresolvedImportException {
        store = EasyMock.createMock(MetaDataStore.class);
        EasyMock.expect(store.resolveDependentContributions(contributionUri)).andReturn(Collections.singleton(dependentContribution));
    }

    private void createContributions() throws MalformedURLException {
        contributionUri = URI.create("contribution");
        contribution = new Contribution(contributionUri);

        dependentUri = URI.create("dependentUri");
        dependentContribution = new Contribution(dependentUri);
    }
}
