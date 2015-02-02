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
package org.fabric3.api.binding.jms.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;

/**
 * A JMS binding configuration element that contains properties.
 */
public abstract class PropertyAwareObject extends ModelObject {
    private Map<String, String> properties = null;

    /**
     * Add a property.
     *
     * @param name  name of the property.
     * @param value value of the property.
     */
    public void addProperty(String name, String value) {
        ensurePropertiesNotNull();
        properties.put(name, value);
    }


    /**
     * Add a property.
     *
     * @param properties the properties.
     */
    public void addProperties(Map<String, String> properties) {
        ensurePropertiesNotNull();
        this.properties.putAll(properties);
    }

    /**
     * Returns properties used to create the administered object.
     *
     * @return Properties used to create the administered object.
     */
    public Map<String, String> getProperties() {
        if (this.properties != null) {
            return properties;
        } else {
            return Collections.emptyMap();
        }
    }

    private void ensurePropertiesNotNull() {
        if (properties == null) {
            properties = new HashMap<>();
        }
    }

}
