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
package org.fabric3.implementation.pojo.injection;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.objectfactory.InjectionAttributes;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Returns a <code>Map</code> containing object instances.
 */
public class MapMultiplicityObjectFactory implements MultiplicityObjectFactory<Map<?, ?>> {
    private volatile Map<Object, ObjectFactory<?>> factories = new HashMap<Object, ObjectFactory<?>>();
    private Map<Object, ObjectFactory<?>> temporaryFactories;

    private FactoryState state = FactoryState.UPDATED;

    public Map<Object, Object> getInstance() throws ObjectCreationException {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Map.Entry<Object, ObjectFactory<?>> entry : factories.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getInstance());
        }
        return map;
    }

    public void addObjectFactory(ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Attributes was null");
        }
        if (attributes.getKey() == null) {
            // programming error as null keys are checked during wire resolution
            throw new IllegalArgumentException("Attributes was null");
        }
        if (state != FactoryState.UPDATING) {
            throw new IllegalStateException("Factory not in updating state. The method startUpdate() must be called first.");
        }
        temporaryFactories.put(attributes.getKey(), objectFactory);
    }

    public void clear() {
        factories.clear();
    }

    public void startUpdate() {
        state = FactoryState.UPDATING;
        temporaryFactories = new HashMap<Object, ObjectFactory<?>>();
    }

    public void endUpdate() {
        if (temporaryFactories != null && !temporaryFactories.isEmpty()) {
            // The isEmpty() check ensures only updates are applied since startUpdate()/endUpdate() can be called if there are no changes present.
            // Otherwise, if no updates are made, existing factories will be overwritten by the empty collection.
            factories = temporaryFactories;
            temporaryFactories = null;
        }
        state = FactoryState.UPDATED;
    }


}
