/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.drools.runtime;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.drools.provision.DroolsSourceDefinition;
import org.fabric3.implementation.pojo.builder.KeyInstantiationException;
import org.fabric3.implementation.pojo.builder.ProxyCreationException;
import org.fabric3.implementation.pojo.builder.WireProxyService;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches and detaches wires to and from Drools components.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DroolsSourceWireAttacher implements SourceWireAttacher<DroolsSourceDefinition> {
    private ComponentManager manager;
    private WireProxyService proxyService;
    private ClassLoaderRegistry classLoaderRegistry;

    public DroolsSourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference WireProxyService proxyService,
                                    @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(DroolsSourceDefinition sourceDefinition, PhysicalTargetDefinition targetDefinition, Wire wire) throws WiringException {
        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        DroolsComponent source = (DroolsComponent) manager.getComponent(sourceName);
        if (source == null) {
            throw new WiringException("Source callback not found: " + sourceName);
        }
        String identifier = sourceDefinition.getIdentifier();

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(sourceDefinition.getClassLoaderId(), sourceDefinition.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class: " + name, sourceUri, null, e);
        }
        if (InjectableType.CALLBACK.equals(sourceDefinition.getInjectableType())) {
            processCallback(wire, targetDefinition, source, identifier, type);
        } else {
            processReference(wire, targetDefinition, source, identifier, type);
        }
    }

    public void detach(DroolsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        detachObjectFactory(source, target);
    }

    public void detachObjectFactory(DroolsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        DroolsComponent component = (DroolsComponent) manager.getComponent(sourceName);
        String identifier = source.getIdentifier();
        component.removeObjectFactory(identifier);
    }

    public void attachObjectFactory(DroolsSourceDefinition sourceDefinition, ObjectFactory<?> factory, PhysicalTargetDefinition targetDefinition)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        DroolsComponent sourceComponent = (DroolsComponent) manager.getComponent(sourceId);
        String identifier = sourceDefinition.getIdentifier();

        sourceComponent.setObjectFactory(identifier, factory);
    }

    private void processReference(Wire wire,
                                  PhysicalTargetDefinition targetDefinition,
                                  DroolsComponent source,
                                  String identifier,
                                  Class<?> type) throws KeyInstantiationException {
        String callbackUri = null;
        URI uri = targetDefinition.getCallbackUri();
        if (uri != null) {
            callbackUri = uri.toString();
        }

        try {
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, wire, callbackUri);
            source.setObjectFactory(identifier, factory);
        } catch (ProxyCreationException e) {
            throw new KeyInstantiationException(e);
        }
    }

    private void processCallback(Wire wire, PhysicalTargetDefinition targetDefinition, DroolsComponent source, String identifier, Class<?> type)
            throws WiringException {
        URI callbackUri = targetDefinition.getUri();
        ObjectFactory<?> factory = source.getObjectFactory(identifier);
        try {
            if (factory == null) {
                factory = proxyService.createCallbackObjectFactory(type, false, callbackUri, wire);
            } else {
                factory = proxyService.updateCallbackObjectFactory(factory, type, false, callbackUri, wire);
            }
            source.setObjectFactory(identifier, factory);
        } catch (ProxyCreationException e) {
            throw new WiringException(e);
        }
    }

}
