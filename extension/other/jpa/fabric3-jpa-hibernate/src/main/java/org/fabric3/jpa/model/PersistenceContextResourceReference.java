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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.jpa.model;

import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * Represents an entity manager factory treated as a resource.
 */
public final class PersistenceContextResourceReference extends ResourceReference {
    private static final long serialVersionUID = -8717050996527626286L;
    private String unitName;
    private boolean multiThreaded;

    /**
     * Constructor.
     *
     * @param name            Name of the resource.
     * @param unitName        Persistence unit name.
     * @param serviceContract the service contract for the persistence unit
     * @param multiThreaded   true if the resource is accessed from a multi-threaded implementation
     */
    public PersistenceContextResourceReference(String name,
                                               String unitName,
                                               ServiceContract serviceContract,
                                               boolean multiThreaded) {
        super(name, serviceContract, true);
        this.unitName = unitName;
        this.multiThreaded = multiThreaded;
    }

    /**
     * Returns the persistence unit name.
     *
     * @return the persistence unit name.
     */
    public final String getUnitName() {
        return this.unitName;
    }

    /**
     * Returns true if the EntityManager will be accessed from a mutli-thread implementation.
     *
     * @return true if the EntityManager will be accessed from a mutli-thread implementation
     */
    public boolean isMultiThreaded() {
        return multiThreaded;
    }

}