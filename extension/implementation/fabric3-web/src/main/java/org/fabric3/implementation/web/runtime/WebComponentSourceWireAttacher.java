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
package org.fabric3.implementation.web.runtime;

import java.net.URI;

import org.fabric3.implementation.web.provision.WebComponentWireSourceDefinition;
import org.fabric3.spi.container.builder.BuildException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.container.wire.Wire;

/**
 * Source WireAttacher for web components.
 */
@EagerInit
public class WebComponentSourceWireAttacher implements SourceWireAttacher<WebComponentWireSourceDefinition> {
    private ComponentManager manager;

    public WebComponentSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(WebComponentWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws BuildException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        try {
            component.attachWire(referenceName, wire);
        } catch (ObjectCreationException e) {
            throw new BuildException(e);
        }
    }

    public void detach(WebComponentWireSourceDefinition source, PhysicalWireTargetDefinition target) throws BuildException {
        // TODO implement
    }

    public void detachObjectFactory(WebComponentWireSourceDefinition source, PhysicalWireTargetDefinition target) throws BuildException {
        // TODO implement
    }

    public void attachObjectFactory(WebComponentWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws BuildException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        try {
            component.attachWire(referenceName, objectFactory);
        } catch (ObjectCreationException e) {
            throw new BuildException(e);
        }
    }
}