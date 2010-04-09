/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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

import java.util.List;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Interface for services that process contributions. Contribution processing occurs in several phases. Contribution metadata is first processed,
 * after which contained resources are indexed. Indexed {@link Resource}s contain 0..n {@link ResourceElement}s, which are addressable parts.
 * ResourceElements contain a key for a symbol space and a value. When a resource is indexed, only ResourceElement keys are available; their values
 * have not yet been loaded.
 * <p/>
 * The final processing phase is when the contribution is loaded. At this point, all contribution artifacts, including those in depedent
 * contributions, are made available through the provided classloader. Indexed Resources are iterated and all ResourceElement values are loaded via
 * the loader framework. As ResourceElements may refer to other ResourceElements, loading may ocurr recursively.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionProcessor {
    /**
     * Returns the content type this implementation handles.
     *
     * @return the content type this implementation handles
     */
    List<String> getContentTypes();

    /**
     * Processses manifest information for the contribution, including imports and exports.
     *
     * @param contribution the contribution that will be used to hold the results from the processing
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem with the contribution
     */
    void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException;

    /**
     * Indexes all contribution resources
     *
     * @param contribution the contribution to index
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing
     */
    void index(Contribution contribution, IntrospectionContext context) throws InstallException;

    /**
     * Loads all resources in the contribution.
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem loading the contribution resoruces
     */
    void process(Contribution contribution, IntrospectionContext context) throws InstallException;

}
