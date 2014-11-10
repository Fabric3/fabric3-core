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
 */
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.util.Set;

import org.fabric3.api.model.type.ModelObject;

/**
 * Dynamically updates a resource element contained in contribution and all references to it, including the transitive set of importing contributions,
 * if any.
 */
public interface ResourceElementUpdater<V extends Serializable> {

    /**
     * Updates the resource element with the new value.
     *
     * @param value                  the new resource element value
     * @param contribution           the containing contribution
     * @param dependentContributions the transitive set of dependent contributions
     * @return the collection of model object that have been changed by the update. For example, an update to a composite will cause changes in other
     *         composites that reference it
     */
    Set<ModelObject> update(V value, Contribution contribution, Set<Contribution> dependentContributions);

    /**
     * Removes a resource element from a contribution. References to the element may be replaced by unresolved pointers depending on the resource
     * type.
     *
     * @param value                  the resource element value to remove
     * @param contribution           the containing contribution
     * @param dependentContributions the transitive set of dependent contributions
     * @return the collection of model object that have been changed by the removal. For example, a deleted composite will cause changes in other
     *         composites that reference it. References to deleted elements may be replaced with pointers.
     */
    Set<ModelObject> remove(V value, Contribution contribution, Set<Contribution> dependentContributions);

}
