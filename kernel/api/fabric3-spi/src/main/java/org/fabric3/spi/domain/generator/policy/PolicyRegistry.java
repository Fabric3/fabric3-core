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
package org.fabric3.spi.domain.generator.policy;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.api.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.api.model.type.definitions.PolicySet;

/**
 * Registry of binding types, implementation types, intents and policy sets within an SCA domain.
 */
public interface PolicyRegistry {

    /**
     * Returns all the definitions of a given type.
     *
     * @param <D>             definition type.
     * @param definitionClass definition class.
     * @return all definitions of the given type.
     */
    <D extends AbstractPolicyDefinition> Collection<D> getAllDefinitions(Class<D> definitionClass);

    /**
     * Returns the definition of the specified type and qualified name.
     *
     * @param <D>             dDefinition type.
     * @param name            qualified name of the definition object.
     * @param definitionClass definition class.
     * @return Requested definition object if available, otherwise null.
     */
    <D extends AbstractPolicyDefinition> D getDefinition(QName name, Class<D> definitionClass);

    /**
     * Returns the definitions of the specified type for the set of qualified names.
     *
     * @param <D>             definition type.
     * @param names           qualified names of the definition object.
     * @param definitionClass definition class.
     * @return Requested definition object if available, otherwise null.
     */
    <D extends AbstractPolicyDefinition> Set<D> getDefinitions(Set<QName> names, Class<D> definitionClass);

    /**
     * Returns a list of active PolicySets that use external attachment.
     *
     * @return the PolicySets
     */
    Set<PolicySet> getExternalAttachmentPolicies();

    /**
     * Activates all the policy definitions in the specified contribution.
     *
     * @param uri The contribution uri.
     * @return the activated policy sets that need to be externally attached
     * @throws PolicyActivationException If unable to find definition.
     */
    Set<PolicySet> activateDefinitions(URI uri) throws PolicyActivationException;

    /**
     * Deactivates all the policy definitions in the specified contribution.
     *
     * @param uri The contribution uri.
     * @return the activated policy sets that need to be externally detached
     * @throws PolicyActivationException If unable to find definition.
     */
    Set<PolicySet> deactivateDefinitions(URI uri) throws PolicyActivationException;

}
