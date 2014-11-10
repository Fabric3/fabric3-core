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
import java.util.Map;
import java.util.Set;

/**
 * Indicates intents or policySet definitions may be associated with a type.
 */
public interface PolicyAware {

    /**
     * Add an intent to the definition.
     *
     * @param intent the intent
     */
    public void addIntent(QName intent);

    /**
     * Returns the intents this definition references.
     *
     * @return the intents this definition references
     */
    Set<QName> getIntents();

    /**
     * Add a policy set to the definition.
     *
     * @param policySet the policy set
     */
    public void addPolicySet(QName policySet);

    /**
     * Returns the policySets this definition references.
     *
     * @return the policySets this definition references
     */
    Set<QName> getPolicySets();

    /**
     * Sets the intents this definition references.
     *
     * @param intents the intents this definition references
     */
    void setIntents(Set<QName> intents);

    /**
     * Returns the policySets this definition references.
     *
     * @param policySets the policySets this definition references
     */
    void setPolicySets(Set<QName> policySets);

    /**
     * Adds intent or policy metadata.
     *
     * @param name qualified name of the intent or policy
     * @param data the metadata
     */
    void addMetadata(QName name, Serializable data);

    /**
     * Adds intent or policy metadata.
     *
     * @param name qualified name of the intent or policy
     * @param type the metadata type
     * @return the metadata or null if not found
     */
    <T> T getMetadata(QName name, Class<T> type);

    /**
     * Returns all policy-related metadata defined for the model object.
     *
     * @return all policy-related metadata
     */
    public Map<QName, Serializable> getMetadata();

}
