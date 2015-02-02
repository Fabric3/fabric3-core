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
package org.fabric3.implementation.pojo.supplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.fabric3.spi.container.injection.InjectionAttributes;

/**
 * Abstract supplier for implementations that return a collection of objects.
 */
public abstract class AbstractCollectionMultiplicitySupplier<T extends Collection<Supplier<?>>> implements MultiplicitySupplier<Object> {
    private static final Comparator<Map.Entry<Supplier<?>, InjectionAttributes>> COMPARATOR = (first, second) -> first.getValue().getOrder()
                                                                                                                 - second.getValue().getOrder();

    protected volatile List<Supplier<?>> suppliers;
    private LinkedHashMap<Supplier<?>, InjectionAttributes> temporarySuppliers;
    private FactoryState state;

    public AbstractCollectionMultiplicitySupplier() {
        this.suppliers = new ArrayList<>();
        state = FactoryState.UPDATED;
    }

    public void addSupplier(Supplier<?> supplier, InjectionAttributes injectionAttributes) {
        if (state != FactoryState.UPDATING) {
            throw new IllegalStateException("Factory not in updating state. The method startUpdate() must be called first.");
        }
        temporarySuppliers.put(supplier, injectionAttributes);
    }

    public void clear() {
        suppliers.clear();
    }

    public void startUpdate() {
        state = FactoryState.UPDATING;
        temporarySuppliers = new LinkedHashMap<>();
    }

    public void endUpdate() {
        if (temporarySuppliers != null && !temporarySuppliers.isEmpty()) {
            // The isEmpty() check ensures only updates are applied since startUpdate()/endUpdate() can be called if there are no changes present.
            // Otherwise, if no updates are made, existing factories will be overwritten by the empty collection.
            suppliers = sortTemporaryFactories(temporarySuppliers);
            temporarySuppliers = null;
        }
        state = FactoryState.UPDATED;
    }

    /**
     * Sorts the factories by {@link InjectionAttributes#getOrder()}.
     *
     * @param factories the factories
     * @return the sorted factories
     */
    private List<Supplier<?>> sortTemporaryFactories(LinkedHashMap<Supplier<?>, InjectionAttributes> factories) {
        List<Map.Entry<Supplier<?>, InjectionAttributes>> entries = new ArrayList<>(factories.entrySet());
        Collections.sort(entries, COMPARATOR);
        return entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

}
