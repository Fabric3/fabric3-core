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
package org.fabric3.xquery.runtime;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.xquery.provision.XQueryComponentWireSourceDefinition;

/**
 *
 */
@EagerInit
public class XQueryComponentSourceWireAttacher implements SourceWireAttacher<XQueryComponentWireSourceDefinition> {

    private ComponentManager manager;

    public XQueryComponentSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(XQueryComponentWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws BuilderException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }
        XQueryComponent component = (XQueryComponent) manager.getComponent(sourceUri);
        component.attachSourceWire(referenceName, callbackUri, wire);
    }

    public void detach(XQueryComponentWireSourceDefinition source, PhysicalWireTargetDefinition target) throws BuilderException {
        throw new AssertionError();
    }

    public void attachObjectFactory(XQueryComponentWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws BuilderException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        XQueryComponent component = (XQueryComponent) manager.getComponent(sourceUri);
        try {
            component.attachObjectFactory(referenceName, objectFactory);
        } catch (ObjectCreationException e) {
            throw new BuilderException(e);
        }
    }

    public void detachObjectFactory(XQueryComponentWireSourceDefinition source, PhysicalWireTargetDefinition target) throws BuilderException {
        throw new AssertionError();
    }

}