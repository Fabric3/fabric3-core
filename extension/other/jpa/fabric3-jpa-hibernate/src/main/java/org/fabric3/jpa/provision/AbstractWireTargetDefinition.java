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
package org.fabric3.jpa.provision;

import java.util.Collections;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Contains general attach point metadata for Hibernate and JPA resources.
 */
public abstract class AbstractWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 2196282576920362492L;
    private String unitName;
    private PersistenceOverrides overrides;

    protected AbstractWireTargetDefinition(String unitName) {
        this.unitName = unitName;
        overrides = new PersistenceOverrides(unitName, Collections.<String, String>emptyMap());
    }

    /**
     * Returns the persistence unit name.
     *
     * @return the persistence unit name
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Returns property overrides for a collection of persistence contexts or Hibernate Sessions.
     *
     * @return property overrides for a collection of persistence contexts or Hibernate Sessions
     */
    public PersistenceOverrides getOverrides() {
        return overrides;
    }

    /**
     * Sets property overrides for a collection of the persistence contexts or Hibernate Sessions.
     *
     * @param overrides property overrides for a collection of persistence contexts or Hibernate Sessions
     */
    public void setOverrides(PersistenceOverrides overrides) {
        this.overrides = overrides;
    }


}