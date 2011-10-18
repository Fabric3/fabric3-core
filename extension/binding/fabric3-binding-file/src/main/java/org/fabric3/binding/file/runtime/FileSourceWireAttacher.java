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
package org.fabric3.binding.file.runtime;

import java.io.File;
import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.file.api.FileBindingAdapter;
import org.fabric3.binding.file.common.Strategy;
import org.fabric3.binding.file.provision.FileBindingSourceDefinition;
import org.fabric3.binding.file.runtime.receiver.PassThroughInterceptor;
import org.fabric3.binding.file.runtime.receiver.ReceiverConfiguration;
import org.fabric3.binding.file.runtime.receiver.ReceiverManager;
import org.fabric3.binding.file.runtime.receiver.ReceiverMonitor;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
@EagerInit
public class FileSourceWireAttacher implements SourceWireAttacher<FileBindingSourceDefinition> {
    private static final FileBindingAdapter ADAPTER = new DefaultFileBindingAdapter();
    private ReceiverManager receiverManager;
    private ClassLoaderRegistry registry;
    private ReceiverMonitor monitor;

    public FileSourceWireAttacher(@Reference ReceiverManager receiverManager,
                                  @Reference ClassLoaderRegistry registry,
                                  @Monitor ReceiverMonitor monitor) {
        this.receiverManager = receiverManager;
        this.registry = registry;
        this.monitor = monitor;
    }

    public void attach(FileBindingSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        String id = source.getUri().toString();

        File location = new File(source.getLocation());
        File errorLocation = getErrorLocation(source);
        File archiveLocation = getArchiveLocation(source);

        String pattern = source.getPattern();
        Strategy strategy = source.getStrategy();

        Interceptor interceptor = new PassThroughInterceptor();
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }

        FileBindingAdapter adapter = instantiateAdaptor(source);

        long delay = source.getDelay();

        ReceiverConfiguration configuration =
                new ReceiverConfiguration(id, location, pattern, strategy, errorLocation, archiveLocation, interceptor, adapter, delay, monitor);
        receiverManager.create(configuration);
    }

    public void detach(FileBindingSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        String id = source.getUri().toString();
        receiverManager.remove(id);
    }

    public void attachObjectFactory(FileBindingSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target) {
        throw new UnsupportedOperationException();

    }

    public void detachObjectFactory(FileBindingSourceDefinition source, PhysicalTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    private File getArchiveLocation(FileBindingSourceDefinition source) {
        File archiveLocation = null;
        String archiveLocationStr = source.getArchiveLocation();
        if (archiveLocationStr != null) {
            archiveLocation = new File(archiveLocationStr);
        }
        return archiveLocation;
    }

    private File getErrorLocation(FileBindingSourceDefinition source) {
        File errorLocation = null;
        String errorLocationStr = source.getErrorLocation();
        if (errorLocationStr != null) {
            errorLocation = new File(errorLocationStr);
        }
        return errorLocation;
    }

    private FileBindingAdapter instantiateAdaptor(FileBindingSourceDefinition source) throws WiringException {
        String adapterClass = source.getAdapterClass();
        if (adapterClass == null) {
            return ADAPTER;
        }
        URI uri = source.getClassLoaderId();
        ClassLoader loader = registry.getClassLoader(uri);
        if (loader == null) {
            // this should not happen
            throw new WiringException("ClassLoader not found: " + uri);
        }
        try {
            return (FileBindingAdapter) loader.loadClass(adapterClass).newInstance();
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        } catch (InstantiationException e) {
            throw new WiringException(e);
        } catch (IllegalAccessException e) {
            throw new WiringException(e);
        }
    }

}
