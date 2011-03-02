/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.contribution.UnresolvedImportException;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Implementations store contribution metadata.
 *
 * @version $Rev$ $Date$
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
     * @throws StoreException if an error storing the metadata occurs
     */
    void store(Contribution contribution) throws StoreException;

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
     * Finds a resource element by its symbol against the given contribution uri. This method assumes the resource and .
     *
     * @param uri    the contribution URI to resolve against
     * @param type   the class representing the resource
     * @param symbol the symbol used to represent the resource element.
     * @return the resource element or null if not found
     * @throws StoreException if an error occurs during resolution
     */
    <S extends Symbol, V extends Serializable> ResourceElement<S, V> find(URI uri, Class<V> type, S symbol) throws StoreException;


    /**
     * Resolves a resource element by its symbol against the given contribution uri. Artifacts referenced by this resource will be resolved.
     *
     * @param uri     the contribution URI to resolve against
     * @param type    the class representing the resource
     * @param symbol  the symbol used to represent the resource element.
     * @param context the context to which validation errors and warnings are reported
     * @return the resource element or null if not found
     * @throws org.fabric3.host.contribution.StoreException
     *          if an error occurs during resolution
     */
    <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(URI uri,
                                                                             Class<V> type,
                                                                             S symbol,
                                                                             IntrospectionContext context) throws StoreException;
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
     * @return a collection of ContributionWires matching the import. For multiplicity imports, the collection may contain 0..N wires. For
     *         non-multiplicity imports (e.g. import.java), the collection will contain 0..1 wires.
     * @throws UnresolvedImportException if the import cannot be resolved
     */
    List<ContributionWire<?, ?>> resolveContributionWires(URI uri, Import imprt) throws UnresolvedImportException;

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
     * Resolves a contribution that provides an extension point. Multiple contributions can provide the same extension point, e.g. contributions that
     * represent different versions of a set of services.
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
