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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.objectfactory;

import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract factory for implementations that return a collection of objects.
 */
public abstract class AbstractCollectionMultiplicityObjectFactory<T extends Collection<ObjectFactory<?>>> implements MultiplicityObjectFactory<Object> {
    private static final InjectionComparator COMPARATOR = new InjectionComparator();

    protected volatile List<ObjectFactory<?>> factories;
    private LinkedHashMap<ObjectFactory<?>, InjectionAttributes> temporaryFactories;
    private FactoryState state;

    public AbstractCollectionMultiplicityObjectFactory() {
        this.factories = new ArrayList<ObjectFactory<?>>();
        state = FactoryState.UPDATED;
    }

    public void addObjectFactory(ObjectFactory<?> objectFactory, InjectionAttributes injectionAttributes) {
        if (state != FactoryState.UPDATING) {
            throw new IllegalStateException("Factory not in updating state. The method startUpdate() must be called first.");
        }
        temporaryFactories.put(objectFactory, injectionAttributes);
    }

    public void clear() {
        factories.clear();
    }

    public void startUpdate() {
        state = FactoryState.UPDATING;
        temporaryFactories = new LinkedHashMap<ObjectFactory<?>, InjectionAttributes>();
    }

    public void endUpdate() {
        if (temporaryFactories != null && !temporaryFactories.isEmpty()) {
            // The isEmpty() check ensures only updates are applied since startUpdate()/endUpdate() can be called if there are no changes present.
            // Otherwise, if no updates are made, existing factories will be overwritten by the empty collection.
            factories = sortTemporaryFactories(temporaryFactories);
            temporaryFactories = null;
        }
        state = FactoryState.UPDATED;
    }

    /**
     * Sorts the factories by {@link InjectionAttributes#getOrder()}.
     *
     * @param factories the factories
     * @return the sorted factories
     */
    private List<ObjectFactory<?>> sortTemporaryFactories(LinkedHashMap<ObjectFactory<?>, InjectionAttributes> factories) {
        List<Map.Entry<ObjectFactory<?>, InjectionAttributes>> entries =
                new ArrayList<Map.Entry<ObjectFactory<?>, InjectionAttributes>>(factories.entrySet());
        Collections.sort(entries, COMPARATOR);
        List<ObjectFactory<?>> sorted = new ArrayList<ObjectFactory<?>>();
        for (Map.Entry<ObjectFactory<?>, InjectionAttributes> entry : entries) {
            sorted.add(entry.getKey());
        }
        return sorted;
    }

    private static class InjectionComparator implements Comparator<Map.Entry<ObjectFactory<?>, InjectionAttributes>> {
        public int compare(Map.Entry<ObjectFactory<?>, InjectionAttributes> first, Map.Entry<ObjectFactory<?>, InjectionAttributes> second) {
            return first.getValue().getOrder() - second.getValue().getOrder();
        }
    }

}
