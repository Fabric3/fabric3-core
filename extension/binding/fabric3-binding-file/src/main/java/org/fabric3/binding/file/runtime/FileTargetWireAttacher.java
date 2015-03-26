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

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.file.ReferenceAdapter;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.file.provision.FileBindingWireTarget;
import org.fabric3.binding.file.runtime.sender.FileSystemInterceptor;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.util.ClassLoading;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
@Key("org.fabric3.binding.file.provision.FileBindingWireTarget")
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class FileTargetWireAttacher implements TargetWireAttacher<FileBindingWireTarget> {
    private static final ReferenceAdapter ADAPTER = new DefaultReferenceAdapter();

    private File baseDir;
    private ComponentManager manager;

    public FileTargetWireAttacher(@Reference ComponentManager manager, @Reference HostInfo hostInfo) {
        this.manager = manager;
        this.baseDir = new File(hostInfo.getDataDir(), "outbox");
    }

    public void attach(PhysicalWireSource source, FileBindingWireTarget target, Wire wire) throws Fabric3Exception {
        File location = resolve(target.getLocation());
        location.mkdirs();

        ReferenceAdapter adapter = getAdaptor(target);
        FileSystemInterceptor interceptor = new FileSystemInterceptor(location, adapter);
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }
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
    private ReferenceAdapter getAdaptor(FileBindingWireTarget source) throws Fabric3Exception {
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

        ClassLoader loader = source.getClassLoader();
        return ClassLoading.instantiate(ReferenceAdapter.class, loader, adapterClass);
    }

}
