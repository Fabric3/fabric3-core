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
package org.fabric3.api.model.type.definitions;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.api.model.type.ModelObject;

/**
 * A policy set intent map.
 */
public final class IntentMap extends ModelObject<PolicySet> {
    private static final long serialVersionUID = -1786000484366117318L;
    private QName provides;
    private Set<IntentQualifier> qualifiers = new HashSet<>();

    public IntentMap(QName provides) {
        this.provides = provides;
    }

    /**
     * Returns the unqualified intent this map provides, which must correspond to an intent in the provides attribute of the parent policy set.
     *
     * @return the unqualified intent
     */
    public QName getProvides() {
        return provides;
    }

    /**
     * Adds an intent qualifier.
     *
     * @param qualifier the qualifier
     */
    public void addQualifier(IntentQualifier qualifier) {
        qualifiers.add(qualifier);
    }

    /**
     * Returns the qualifiers for this map.
     *
     * @return the qualifiers for this map.
     */
    public Set<IntentQualifier> getQualifiers() {
        return qualifiers;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntentMap intentMap = (IntentMap) o;

        return !(provides != null ? !provides.equals(intentMap.provides) : intentMap.provides != null);

    }

    public int hashCode() {
        return provides != null ? provides.hashCode() : 0;
    }
}
