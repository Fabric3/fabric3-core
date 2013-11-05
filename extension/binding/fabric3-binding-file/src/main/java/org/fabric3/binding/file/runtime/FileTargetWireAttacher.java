/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.binding.file.runtime;

import java.io.File;
import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.binding.file.ReferenceAdapter;
import org.fabric3.binding.file.provision.FileBindingTargetDefinition;
import org.fabric3.binding.file.runtime.sender.FileSystemInterceptor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;

/**
 *
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
@EagerInit
public class FileTargetWireAttacher implements TargetWireAttacher<FileBindingTargetDefinition> {
    private static final ReferenceAdapter ADAPTER = new DefaultReferenceAdapter();

    private ClassLoaderRegistry registry;
    private File baseDir;
    private ComponentManager manager;

    public FileTargetWireAttacher(@Reference ClassLoaderRegistry registry, @Reference ComponentManager manager, @Reference HostInfo hostInfo) {
        this.registry = registry;
        this.manager = manager;
        this.baseDir = new File(hostInfo.getDataDir(), "outbox");
    }

    public void attach(PhysicalSourceDefinition source, FileBindingTargetDefinition target, Wire wire) throws WiringException {
        File location = resolve(target.getLocation());
        location.mkdirs();

        ReferenceAdapter adapter = getAdaptor(target);
        FileSystemInterceptor interceptor = new FileSystemInterceptor(location, adapter);
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalSourceDefinition source, FileBindingTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(FileBindingTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
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
     * @throws WiringException if there is an error instantiating the class or returning a component instance.
     */
    private ReferenceAdapter getAdaptor(FileBindingTargetDefinition source) throws WiringException {
        String adapterClass = source.getAdapterClass();
        if (adapterClass == null) {
            URI adapterUri = source.getAdapterUri();
            if (adapterUri == null) {
                return ADAPTER;
            }
            Component component = manager.getComponent(adapterUri);
            if (component == null) {
                throw new WiringException("Binding adaptor component not found: " + adapterUri);
            }
            if (!(component instanceof AtomicComponent)) {
                throw new WiringException("Adaptor component must implement " + AtomicComponent.class.getName() + ": " + adapterUri);
            }
            return new ReferenceAdaptorWrapper((AtomicComponent) component);
        }
        URI uri = source.getClassLoaderId();
        ClassLoader loader = registry.getClassLoader(uri);
        if (loader == null) {
            // this should not happen
            throw new WiringException("ClassLoader not found: " + uri);
        }
        try {
            return (ReferenceAdapter) loader.loadClass(adapterClass).newInstance();
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        } catch (InstantiationException e) {
            throw new WiringException(e);
        } catch (IllegalAccessException e) {
            throw new WiringException(e);
        }
    }

}
