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
 */
package org.fabric3.fabric.container.builder.classloader;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.container.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.model.os.Library;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 *
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
        URI dependentUri = URI.create("dependent");
        ClassLoader classLoader = getClass().getClassLoader();

        // build calls
        EasyMock.expect(tracker.increment(uri)).andReturn(1);
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(null);   // return null since classloader not yet registered
        EasyMock.expect(classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION)).andReturn(classLoader);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(true);
        URL url = new URL("file://test");
        EasyMock.expect(resolver.resolve(uri)).andReturn(url);
        List<URL> classpath = Collections.emptyList();
        EasyMock.expect(processorRegistry.process(url, Collections.<Library>emptyList())).andReturn(classpath);
        classLoaderRegistry.register(EasyMock.eq(uri), EasyMock.isA(ClassLoader.class));
        EasyMock.expectLastCall();
        wireBuilder.build(EasyMock.isA(MultiParentClassLoader.class), EasyMock.isA(PhysicalClassLoaderWireDefinition.class));
        EasyMock.expect(classLoaderRegistry.getClassLoader(dependentUri)).andReturn(classLoader);
        tracker.incrementImported(EasyMock.isA(ClassLoader.class));

        // destroy calls 
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(classLoader);
        EasyMock.expect(tracker.decrement(classLoader)).andReturn(0);
        EasyMock.expect(store.find(uri)).andReturn(null);   // simulates a participant which does not introspect application contributions
        EasyMock.expect(classLoaderRegistry.unregister(uri)).andReturn(classLoader); // unregister must be called
        resolver.release(uri);

        EasyMock.replay(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);

        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri, true);
        PhysicalClassLoaderWireDefinition wireDefinition = new PhysicalClassLoaderWireDefinition(dependentUri, "org.fabric3.test");
        definition.add(wireDefinition);
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
        List<URL> classpath = Collections.emptyList();
        EasyMock.expect(processorRegistry.process(url, Collections.<Library>emptyList())).andReturn(classpath);
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

    public void testBuildCommonClassLoader() throws Exception {
        URI uri = URI.create("test");
        URI dependentUri = URI.create("dependent");
        ClassLoader classLoader = getClass().getClassLoader();

        // build calls
        EasyMock.expect(tracker.increment(uri)).andReturn(1);
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(null);   // return null since classloader not yet registered
        EasyMock.expect(classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION)).andReturn(classLoader);
        EasyMock.expect(info.supportsClassLoaderIsolation()).andReturn(false);
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
        PhysicalClassLoaderWireDefinition wireDefinition = new PhysicalClassLoaderWireDefinition(dependentUri, "org.fabric3.test");
        definition.add(wireDefinition);
        builder.build(definition);
        builder.destroy(uri);

        EasyMock.verify(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);

    }

    public void testClassLoaderAlreadyRegistered() throws Exception {
        URI uri = URI.create("test");
        URI dependentUri = URI.create("dependent");
        ClassLoader classLoader = getClass().getClassLoader();

        // build calls
        EasyMock.expect(tracker.increment(uri)).andReturn(1);
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(classLoader);   // simulate registered classloader
        EasyMock.expect(classLoaderRegistry.getClassLoader(dependentUri)).andReturn(classLoader);
        tracker.incrementImported(EasyMock.isA(ClassLoader.class));

        // destroy calls
        EasyMock.expect(classLoaderRegistry.getClassLoader(uri)).andReturn(classLoader);
        EasyMock.expect(tracker.decrement(classLoader)).andReturn(0);
        EasyMock.expect(store.find(uri)).andReturn(null);   // simulates a participant which does not introspect application contributions
        EasyMock.expect(classLoaderRegistry.unregister(uri)).andReturn(classLoader); // unregister must be called
        resolver.release(uri);

        EasyMock.replay(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info);

        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri, true);
        PhysicalClassLoaderWireDefinition wireDefinition = new PhysicalClassLoaderWireDefinition(dependentUri, "org.fabric3.test");
        definition.add(wireDefinition);
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
        ClassLoaderBuilderMonitor monitor = EasyMock.createNiceMock(ClassLoaderBuilderMonitor.class);

        builder = new ClassLoaderBuilderImpl(wireBuilder, classLoaderRegistry, processorRegistry, resolver, tracker, store, info, monitor);
    }
}
