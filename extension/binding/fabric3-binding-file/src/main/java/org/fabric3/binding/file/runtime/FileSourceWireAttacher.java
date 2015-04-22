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
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.file.provision.FileBindingWireSource;
import org.fabric3.binding.file.runtime.receiver.PassThroughInterceptor;
import org.fabric3.binding.file.runtime.receiver.ReceiverConfiguration;
import org.fabric3.binding.file.runtime.receiver.ReceiverManager;
import org.fabric3.binding.file.runtime.receiver.ReceiverMonitor;
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.util.ClassLoading;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
@Key("org.fabric3.binding.file.provision.FileBindingWireSource")
public class FileSourceWireAttacher implements SourceWireAttacher<FileBindingWireSource> {
    private static final ServiceAdapter ADAPTER = new DefaultServiceAdapter();
    private static final ServiceAdapter JAF_ADAPTER = new DataHandlerServiceAdapter();

    private ReceiverManager receiverManager;
    private ComponentManager manager;
    private ReceiverMonitor monitor;
    private File baseDir;

    public FileSourceWireAttacher(@Reference ReceiverManager receiverManager,
                                  @Reference ComponentManager manager,
                                  @Reference HostInfo hostInfo,
                                  @Monitor ReceiverMonitor monitor) {
        this.receiverManager = receiverManager;
        this.manager = manager;
        this.monitor = monitor;
        this.baseDir = new File(hostInfo.getDataDir(), "inbox");
    }

    public void attach(FileBindingWireSource source, PhysicalWireTarget target, Wire wire) {
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

    public void detach(FileBindingWireSource source, PhysicalWireTarget target) {
        String id = source.getUri().toString();
        receiverManager.remove(id);
    }

    private File getLocation(FileBindingWireSource source) {
        String location = source.getLocation();
        return resolve(location);
    }

    private File getArchiveLocation(FileBindingWireSource source) {
        File archiveLocation = null;
        String archiveLocationStr = source.getArchiveLocation();
        if (archiveLocationStr != null) {
            archiveLocation = resolve(archiveLocationStr);
        }
        return archiveLocation;
    }

    private File getErrorLocation(FileBindingWireSource source) {
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
     * @throws Fabric3Exception if there is an error instantiating the class or returning a component instance.
     */
    private ServiceAdapter getAdaptor(FileBindingWireSource source) throws Fabric3Exception {
        String adapterClass = source.getAdapterClass();
        if (adapterClass == null) {
            URI adapterUri = source.getAdapterUri();
            if (adapterUri == null) {
                return source.isDataHandler() ? JAF_ADAPTER : ADAPTER;
            }
            Component component = manager.getComponent(adapterUri);
            if (component == null) {
                throw new Fabric3Exception("Binding adaptor component not found: " + adapterUri);
            }
            if (!(component instanceof AtomicComponent)) {
                throw new Fabric3Exception("Adaptor component must implement " + AtomicComponent.class.getName() + ": " + adapterUri);
            }
            return new ServiceAdaptorWrapper((AtomicComponent) component);
        }
        ClassLoader loader = source.getClassLoader();
        return ClassLoading.instantiate(ServiceAdapter.class, loader, adapterClass);
    }

}
