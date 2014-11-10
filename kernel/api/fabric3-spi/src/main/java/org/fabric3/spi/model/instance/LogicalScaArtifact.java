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
package org.fabric3.spi.model.instance;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all logical artifacts.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public abstract class LogicalScaArtifact<P extends LogicalScaArtifact<?>> implements Serializable {
    private static final long serialVersionUID = 3937960041374196627L;
    private P parent;
    private Set<QName> intents = new LinkedHashSet<>();
    private Set<QName> policySets = new LinkedHashSet<>();
    private Map<String, Object> metadata;

    /**
     * Constructor.
     *
     * @param parent Parent of the SCA artifact.
     */
    protected LogicalScaArtifact(P parent) {
        this.parent = parent;
    }

    /**
     * @return Parent of this SCA artifact.
     */
    public final P getParent() {
        return parent;
    }

    public Set<QName> getIntents() {
        return intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public void addIntents(Set<QName> intents) {
        this.intents.addAll(intents);
    }

    public void addPolicySet(QName policySet) {
        policySets.add(policySet);
    }

    public void addPolicySets(Set<QName> policySets) {
        this.policySets.addAll(policySets);
    }

    public void removePolicySet(QName policySet) {
        policySets.remove(policySet);
    }

    public void addMetadata(String key, Object data) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, data);
    }

    public <T> T getMetadata(String key, Class<T> type) {
        if (metadata == null) {
            return null;
        }
        return type.cast(metadata.get(key));
    }

}
