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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
        this.factories = new ArrayList<>();
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
        temporaryFactories = new LinkedHashMap<>();
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
                new ArrayList<>(factories.entrySet());
        Collections.sort(entries, COMPARATOR);
        List<ObjectFactory<?>> sorted = new ArrayList<>();
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
