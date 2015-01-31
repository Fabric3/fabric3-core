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
package org.fabric3.binding.file.runtime;

import java.io.File;
import java.net.URI;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.file.provision.FileBindingWireSourceDefinition;
import org.fabric3.binding.file.runtime.receiver.PassThroughInterceptor;
import org.fabric3.binding.file.runtime.receiver.ReceiverConfiguration;
import org.fabric3.binding.file.runtime.receiver.ReceiverManager;
import org.fabric3.binding.file.runtime.receiver.ReceiverMonitor;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class FileSourceWireAttacher implements SourceWireAttacher<FileBindingWireSourceDefinition> {
    private static final ServiceAdapter ADAPTER = new DefaultServiceAdapter();
    private static final ServiceAdapter JAF_ADAPTER = new DataHandlerServiceAdapter();

    private ReceiverManager receiverManager;
    private ClassLoaderRegistry registry;
    private ComponentManager manager;
    private ReceiverMonitor monitor;
    private File baseDir;

    public FileSourceWireAttacher(@Reference ReceiverManager receiverManager,
                                  @Reference ClassLoaderRegistry registry,
                                  @Reference ComponentManager manager,
                                  @Reference HostInfo hostInfo,
                                  @Monitor ReceiverMonitor monitor) {
        this.receiverManager = receiverManager;
        this.registry = registry;
        this.manager = manager;
        this.monitor = monitor;
        this.baseDir = new File(hostInfo.getDataDir(), "inbox");
    }

    public void attach(FileBindingWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        String id = source.getUri().toString();

        File location = getLocation(source);
        File errorLocation = getErrorLocation(source);
        File archiveLocation = getArchiveLocation(source);

        String pattern = source.getPattern();
        Strategy strategy = source.getStrategy();

        Interceptor interceptor = new PassThroughInterceptor();
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }

        ServiceAdapter adapter = getAdaptor(source);

        long delay = source.getDelay();

        ReceiverConfiguration configuration = new ReceiverConfiguration(id,
                                                                        location,
                                                                        pattern,
                                                                        strategy,
                                                                        errorLocation,
                                                                        archiveLocation,
                                                                        interceptor,
                                                                        adapter,
                                                                        delay,
                                                                        monitor);
        receiverManager.create(configuration);
    }

    public void detach(FileBindingWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        String id = source.getUri().toString();
        receiverManager.remove(id);
    }

    public void attachObjectFactory(FileBindingWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();

    }

    public void detachObjectFactory(FileBindingWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    private File getLocation(FileBindingWireSourceDefinition source) {
        String location = source.getLocation();
        return resolve(location);
    }

    private File getArchiveLocation(FileBindingWireSourceDefinition source) {
        File archiveLocation = null;
        String archiveLocationStr = source.getArchiveLocation();
        if (archiveLocationStr != null) {
            archiveLocation = resolve(archiveLocationStr);
        }
        return archiveLocation;
    }

    private File getErrorLocation(FileBindingWireSourceDefinition source) {
        File errorLocation = null;
        String errorLocationStr = source.getErrorLocation();
        if (errorLocationStr != null) {
            errorLocation = resolve(errorLocationStr);
        }
        return errorLocation;
    }

    /**
     * Resolve the location as an absolute address or relative to the runtime data/inbox directory.
     *
     * @param location the location
     * @return the resolved location
     */
    private File resolve(String location) {
        File file = new File(location);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(baseDir, location);
    }

    /**
     * Instantiates an adaptor class or returns a component instance.
     *
     * @param source the definition
     * @return the adaptor
     * @throws ContainerException if there is an error instantiating the class or returning a component instance.
     */
    private ServiceAdapter getAdaptor(FileBindingWireSourceDefinition source) throws ContainerException {
        String adapterClass = source.getAdapterClass();
        if (adapterClass == null) {
            URI adapterUri = source.getAdapterUri();
            if (adapterUri == null) {
                return source.isDataHandler() ? JAF_ADAPTER : ADAPTER;
            }
            Component component = manager.getComponent(adapterUri);
            if (component == null) {
                throw new ContainerException("Binding adaptor component not found: " + adapterUri);
            }
            if (!(component instanceof AtomicComponent)) {
                throw new ContainerException("Adaptor component must implement " + AtomicComponent.class.getName() + ": " + adapterUri);
            }
            return new ServiceAdaptorWrapper((AtomicComponent) component);
        }
        URI uri = source.getClassLoaderId();
        ClassLoader loader = registry.getClassLoader(uri);
        if (loader == null) {
            // this should not happen
            throw new ContainerException("ClassLoader not found: " + uri);
        }
        try {
            return (ServiceAdapter) loader.loadClass(adapterClass).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new ContainerException(e);
        }
    }

}
