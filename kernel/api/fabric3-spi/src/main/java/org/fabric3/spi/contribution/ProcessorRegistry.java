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

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Manages and dispatches to {@link ContributionProcessor}s and {@link ResourceProcessor}s when introspecting a contribution.
 *
 * @version $Rev$ $Date$
 */
public interface ProcessorRegistry {
    /**
     * Register a ContributionProcessor using the content type as the key
     *
     * @param processor the processor to register
     */
    void register(ContributionProcessor processor);

    /**
     * Unregister a ContributionProcessor for a content type
     *
     * @param processor the processor to unregister
     */
    void unregisterContributionProcessor(ContributionProcessor processor);

    /**
     * Register a ResourceProcessor using the content type as the key
     *
     * @param processor the processor to register
     */
    void register(ResourceProcessor processor);

    /**
     * Unregister a ResourceProcessor for a content type
     *
     * @param contentType the content
     */
    void unregisterResourceProcessor(String contentType);

    /**
     * Dispatches to a {@link ContributionProcessor} to process manifest information in a contribution.
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem processing the manifest
     */
    void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException;

    /**
     * Dispatches to a {@link ContributionProcessor} to index a contribution.
     *
     * @param contribution the contribution to index
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing the contribution
     */
    void indexContribution(Contribution contribution, IntrospectionContext context) throws InstallException;

    /**
     * Dispatches to a {@link ResourceProcessor} to index a resource contained in a contribution.
     *
     * @param contribution the containing contribution
     * @param contentType  the content type of the resource to process
     * @param source       provides an input stream reading the contents of the resource
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing the contribution
     */
    void indexResource(Contribution contribution, String contentType, Source source, IntrospectionContext context) throws InstallException;

    /**
     * Loads all indexed resources in a contribution.
     *
     * @param contribution The contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem loading resources in the contribution
     */
    void processContribution(Contribution contribution, IntrospectionContext context) throws InstallException;

    /**
     * Loads a contained resource in a contribution.
     *
     * @param resource the resource to process
     * @param context  the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem loading the resoure
     */
    void processResource(Resource resource, IntrospectionContext context) throws InstallException;

}
