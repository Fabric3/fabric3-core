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
*/
package org.fabric3.fabric.builder.classloader;

import java.net.URI;
import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 * @version $Rev$ $Date$
 */
public class ClassLoaderBuilderImplTestCase extends TestCase {

    private ClassLoaderBuilderImpl builder;
    private ClassLoaderWireBuilder wireBuilder;
    private ClassLoaderRegistry classLoaderRegistry;
    private ClasspathProcessorRegistry processorRegistry;
    private ContributionResolver resolver;
    private ClassLoaderTracker tracker;
    private MetaDataStore store;
    private HostInfo info;

    public void testMultiVMClassLoaderBuildDestroy() throws Exception {
        URI uri = URI.create("test");
        ClassLoader classLoader = getClass().getClassLoader();

        // build calls
        EasyMock.expect(tracker.increment(uri)).andReturn(1);
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(null);   // return null since classloader not yet registered
        EasyMock.expect(classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION)).andReturn(classLoader);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        URL url = new URL("file://test");
        EasyMock.expect(resolver.resolve(uri)).andReturn(url);
        EasyMock.expect(processorRegistry.process(url)).andReturn(Collections.<URL>emptyList());
        classLoaderRegistry.register(EasyMock.eq(uri), EasyMock.isA(ClassLoader.class));
        EasyMock.expectLastCall();

        // destroy calls 
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(classLoader);
        EasyMock.expect(tracker.decrement(classLoader)).andReturn(0);
        EasyMock.expect(store.find(uri)).andReturn(null);   // simulates a participant which does not introspect application contributions
        EasyMock.expect(classLoaderRegistry.unregister(uri)).andReturn(classLoader); // unregister must be called
        resolver.release(uri);

        EasyMock.replay(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);

        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri, true);
        builder.build(definition);
        builder.destroy(uri);

        EasyMock.verify(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);
    }

    public void testSingleVMClassLoaderBuildDestroy() throws Exception {
        URI uri = URI.create("test");
        ClassLoader classLoader = getClass().getClassLoader();

        // build calls
        EasyMock.expect(tracker.increment(uri)).andReturn(1);
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(null);   // return null since classloader not yet registered
        EasyMock.expect(classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION)).andReturn(classLoader);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        URL url = new URL("file://test");
        EasyMock.expect(resolver.resolve(uri)).andReturn(url);
        EasyMock.expect(processorRegistry.process(url)).andReturn(Collections.<URL>emptyList());
        classLoaderRegistry.register(EasyMock.eq(uri), EasyMock.isA(ClassLoader.class));
        EasyMock.expectLastCall();

        // destroy calls
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(classLoader);
        EasyMock.expect(tracker.decrement(classLoader)).andReturn(0);
        EasyMock.expect(store.find(uri)).andReturn(new Contribution(uri));   // simulates a single VM which does introspect application contributions
          // no ClassloaderRegistry unregister call for single VMs
        EasyMock.replay(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);

        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri, true);
        builder.build(definition);
        builder.destroy(uri);

        EasyMock.verify(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireBuilder = EasyMock.createMock(ClassLoaderWireBuilder.class);
        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        processorRegistry = EasyMock.createMock(ClasspathProcessorRegistry.class);
        resolver = EasyMock.createMock(ContributionResolver.class);
        tracker = EasyMock.createMock(ClassLoaderTracker.class);
        store = EasyMock.createMock(MetaDataStore.class);
        info = EasyMock.createMock(HostInfo.class);
        builder = new ClassLoaderBuilderImpl(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);
    }
}
