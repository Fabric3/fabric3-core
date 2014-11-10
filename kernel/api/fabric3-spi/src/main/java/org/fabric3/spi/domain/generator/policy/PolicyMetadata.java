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
package org.fabric3.spi.domain.generator.policy;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds metadata for intents and policy sets. Some intents and policy sets, such as role-based authorization, require specific configuration (e.g.
 * roles specified by the intent annotation on a component). Metadata is keyed by intent or policy set qualified name.
 */
public class PolicyMetadata {
    private Map<QName, Serializable> metadata = new HashMap<>();

    public void add(QName name, Serializable data) {
        metadata.put(name, data);
    }

    /**
     * Returns the metadata associated with the qname.
     *
     * @param name the intent/policy set qname
     * @param type the expected metadata type
     * @param <T>  the metadata type parameter
     * @return the metadata or null;
     */
    public <T> T get(QName name, Class<T> type) {
        return type.cast(metadata.get(name));
    }

    /**
     * Returns all metadata.
     *
     * @return the metadata
     */
    public Map<QName, Serializable> get() {
        return metadata;
    }

    /**
     * Convenience method for adding a collection of metadata.
     *
     * @param metadata the metadata to add
     */
    public void addAll(Map<QName, Serializable> metadata) {
        this.metadata.putAll(metadata);
    }

}
