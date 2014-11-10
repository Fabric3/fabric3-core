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
package org.fabric3.jpa.api;

import java.io.Serializable;
import java.util.Map;

/**
 * Contains override information for persistence units.
 */
public class PersistenceOverrides implements Serializable {
    private static final long serialVersionUID = -8553765085228025466L;

    private String unitName;
    Map<String, String> properties;

    public PersistenceOverrides(String unitName, Map<String, String> properties) {
        this.unitName = unitName;
        this.properties = properties;
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
     * Returns property overrides for the persistence context or Hibernate Session.
     *
     * @return property overrides for the persistence context or Hibernate Session
     */
    public Map<String, String> getProperties() {
        return properties;
    }

}