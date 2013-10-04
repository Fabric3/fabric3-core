/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.spi.deployment.generator.policy;

import java.net.URI;
import java.util.Collection;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.model.type.definitions.PolicySet;

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
