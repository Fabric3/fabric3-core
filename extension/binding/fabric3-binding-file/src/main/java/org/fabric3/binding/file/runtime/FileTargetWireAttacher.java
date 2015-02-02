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

import org.fabric3.api.binding.file.ReferenceAdapter;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.file.provision.FileBindingWireTargetDefinition;
import org.fabric3.binding.file.runtime.sender.FileSystemInterceptor;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
@EagerInit
public class FileTargetWireAttacher implements TargetWireAttacher<FileBindingWireTargetDefinition> {
    private static final ReferenceAdapter ADAPTER = new DefaultReferenceAdapter();

    private ClassLoaderRegistry registry;
    private File baseDir;
    private ComponentManager manager;

    public FileTargetWireAttacher(@Reference ClassLoaderRegistry registry, @Reference ComponentManager manager, @Reference HostInfo hostInfo) {
        this.registry = registry;
        this.manager = manager;
        this.baseDir = new File(hostInfo.getDataDir(), "outbox");
    }

    public void attach(PhysicalWireSourceDefinition source, FileBindingWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        File location = resolve(target.getLocation());
        location.mkdirs();

        ReferenceAdapter adapter = getAdaptor(target);
        FileSystemInterceptor interceptor = new FileSystemInterceptor(location, adapter);
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalWireSourceDefinition source, FileBindingWireTargetDefinition target) throws Fabric3Exception {
        // no-op
    }

    /**
     * Resolve the location as an absolute address or relative to the runtime data/outbox directory.
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
    private ReferenceAdapter getAdaptor(FileBindingWireTargetDefinition source) throws Fabric3Exception {
        String adapterClass = source.getAdapterClass();
        if (adapterClass == null) {
            URI adapterUri = source.getAdapterUri();
            if (adapterUri == null) {
                return ADAPTER;
            }
            Component component = manager.getComponent(adapterUri);
            if (component == null) {
                throw new Fabric3Exception("Binding adaptor component not found: " + adapterUri);
            }
            if (!(component instanceof AtomicComponent)) {
                throw new Fabric3Exception("Adaptor component must implement " + AtomicComponent.class.getName() + ": " + adapterUri);
            }
            return new ReferenceAdaptorWrapper((AtomicComponent) component);
        }
        URI uri = source.getClassLoaderId();
        ClassLoader loader = registry.getClassLoader(uri);
        if (loader == null) {
            // this should not happen
            throw new Fabric3Exception("ClassLoader not found: " + uri);
        }
        try {
            return (ReferenceAdapter) loader.loadClass(adapterClass).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new Fabric3Exception(e);
        }
    }

}
