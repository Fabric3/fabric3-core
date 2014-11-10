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
package org.fabric3.management.rest.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * A resource that is to be serialized to a specific representation such as JSON.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class Resource implements Serializable {
    private static final long serialVersionUID = -7831162074975555876L;

    private SelfLink selfLink;
    private Map<String, Object> properties;

    public Resource() {
    }

    public Resource(SelfLink selfLink) {
        this.selfLink = selfLink;
    }

    /**
     * Returns the URL for this resource.
     *
     * @return the URL for this resource
     */
    public SelfLink getSelfLink() {
        return selfLink;
    }

    /**
     * Sets the URL for this resource.
     *
     * @param selfLink URL for this resource
     */
    public void setSelfLink(SelfLink selfLink) {
        this.selfLink = selfLink;
    }

    /**
     * Returns extensible properties for the resource.
     *
     * @return extensible properties for the resource
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets an extensible property for the resource.
     *
     * @param key   the property name
     * @param value the property value
     */
    @JsonAnySetter
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }

}
