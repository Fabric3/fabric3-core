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
package org.fabric3.api.model.type;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for types that intents and policySets may be associated with.
 */
public abstract class AbstractPolicyAware<P extends ModelObject> extends ModelObject<P> implements PolicyAware {
    private static final long serialVersionUID = -3494285576822641528L;

    private Set<QName> intents = new LinkedHashSet<>();
    private Set<QName> policySets = new LinkedHashSet<>();
    private Map<QName, Serializable> metadata = new HashMap<>();

    public Set<QName> getIntents() {
        return intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void setIntents(Set<QName> intents) {
        this.intents = intents;
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public void addIntents(Set<QName> intents) {
        this.intents.addAll(intents);
    }

    public void setPolicySets(Set<QName> policySets) {
        this.policySets = policySets;
    }

    public void addPolicySet(QName policySet) {
        policySets.add(policySet);
    }

    public void addPolicySets(Set<QName> policySets) {
        this.policySets.addAll(policySets);
    }

    public void addMetadata(QName name, Serializable data) {
        metadata.put(name, data);
    }

    public <T> T getMetadata(QName name, Class<T> type) {
        return type.cast(metadata.get(name));
    }

    public Map<QName, Serializable> getMetadata() {
        return metadata;
    }
}
