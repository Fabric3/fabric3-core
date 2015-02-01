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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Implementations store contribution metadata.
 */
public interface MetaDataStore {

    /**
     * Returns the installed contributions in the domain.
     *
     * @return the installed contributions in the domain
     */
    Set<Contribution> getContributions();

    /**
     * Stores the contribution metadata
     *
     * @param contribution the contribution metadata
     * @throws Fabric3Exception if an error storing the metadata occurs
     */
    void store(Contribution contribution) throws Fabric3Exception;

    /**
     * Removes the contribution metadata.
     *
     * @param uri the contribution uri
     */
    void remove(URI uri);

    /**
     * Returns the contribution for the given URI.
     *
     * @param uri the contribution URI
     * @return the contribution for the given URI or null if not found
     */
    Contribution find(URI uri);

    /**
     * Finds a resource element by its symbol against the entire domain symbol space.
     *
     * @param type   the class representing the resource
     * @param symbol the symbol used to represent the resource element.
     * @return the resource element or null if not found
     */
    <S extends Symbol, V extends Serializable> ResourceElement<S, V> find(Class<V> type, S symbol);

    /**
     * Finds a resource element by its symbol against the given contribution uri.
     *
     * @param uri    the contribution URI to resolve against
     * @param type   the class representing the resource
     * @param symbol the symbol used to represent the resource element.
     * @return the resource element or null if not found
     * @throws Fabric3Exception if an error occurs during resolution
     */
    <S extends Symbol, V extends Serializable> ResourceElement<S, V> find(URI uri, Class<V> type, S symbol) throws Fabric3Exception;

    /**
     * Returns a collection of resource elements that reference the artifact represented by the given symbol.
     *
     * @param uri    the URI of the contribution to use as the search context
     * @param symbol the artifact symbol
     * @return the set of resources that reference the artifact
     * @throws Fabric3Exception if an error occurs during resolution
     */
    <S extends Symbol> Set<ResourceElement<S, ?>> findReferences(URI uri, S symbol) throws Fabric3Exception;

    /**
     * Updates a resource element contained in a contribution. All references to the resource will be updated in the containing and dependent contributions.
     *
     * @param uri   the contribution URI
     * @param value the new resource element value
     * @return the collection of model object that have been changed by the update. For example, an update to a composite will cause changes in other composites
     * that reference it
     * @throws Fabric3Exception if an error occurs during update
     */
    <V extends Serializable> Set<ModelObject> update(URI uri, V value) throws Fabric3Exception;

    /**
     * Removes a resource element from a contribution. References to the element may be replaced by unresolved pointers depending on the resource type.
     *
     * @param uri   the contribution URI
     * @param value the new resource element value
     * @return the collection of model object that have been changed by the removal. For example, a deleted composite will cause changes in other composites
     * that reference it. References to deleted elements may be replaced with pointers.
     * @throws Fabric3Exception if an error occurs during update
     */
    <V extends Serializable> Set<ModelObject> remove(URI uri, V value) throws Fabric3Exception;

    /**
     * Resolves a resource element by its symbol against the given contribution uri. Artifacts referenced by this resource will be resolved.
     *
     * @param uri     the contribution URI to resolve against
     * @param type    the class representing the resource
     * @param symbol  the symbol used to represent the resource element.
     * @param context the context to which validation errors and warnings are reported
     * @return the resource element or null if not found
     * @throws Fabric3Exception if an error occurs during resolution
     */
    <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(URI uri, Class<V> type, S symbol, IntrospectionContext context)
            throws Fabric3Exception;

    /**
     * Resolves resource elements for a given type that are visible to the contribution.
     *
     * @param uri  the contribution to search
     * @param type the resource element type
     * @return the collection of resource elements
     * @throws Fabric3Exception if there is an error resolving the resource elements
     */
    <V extends Serializable> List<ResourceElement<?, V>> resolve(URI uri, Class<V> type) throws Fabric3Exception;

    /**
     * Resolves an import or returns an empty list if it cannot be satisfied.
     *
     * @param uri   the importing contribution  URI
     * @param imprt the import
     * @return the contributions
     */
    List<Contribution> resolve(URI uri, Import imprt);

    /**
     * Resolves an import to a matching export and returns the associated ContributionWire.
     *
     * @param uri   the importing contribution  URI
     * @param imprt the import to resolve @return a ContributionWire or null
     * @return a collection of ContributionWires matching the import. For multiplicity imports, the collection may contain 0..N wires. For non-multiplicity
     * imports (e.g. import.java), the collection will contain 0..1 wires.
     */
    List<ContributionWire<?, ?>> resolveContributionWires(URI uri, Import imprt) throws Fabric3Exception;

    /**
     * Resolves contributions that import the contribution represented by the given URI.
     *
     * @param uri the contribution URI
     * @return the set of contributions that import the contribution
     */
    Set<Contribution> resolveDependentContributions(URI uri);

    /**
     * Resolves the contributions that extend an extension point.
     *
     * @param name the extension point name
     * @return the contributions that extend and extension point
     */
    List<Contribution> resolveExtensionProviders(String name);

    /**
     * Resolves a contribution that provides an extension point. Multiple contributions can provide the same extension point, e.g. contributions that represent
     * different versions of a set of services.
     *
     * @param name the extension point name
     * @return the URIs of the contributions that provide the extension points
     */
    List<Contribution> resolveExtensionPoints(String name);

    /**
     * Transitively resolves the extensions that provide capabilities required by the given contribution.
     *
     * @param contribution the contribution
     * @return the extensions
     */
    Set<Contribution> resolveCapabilities(Contribution contribution);

    /**
     * Transitively resolves the extensions that provide the given capability.
     *
     * @param capability the capability
     * @return the extensions
     */
    Set<Contribution> resolveCapability(String capability);

}
