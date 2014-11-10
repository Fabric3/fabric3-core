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

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Manages and dispatches to {@link ContributionProcessor}s and {@link ResourceProcessor}s when introspecting a contribution.
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
    void unregister(ContributionProcessor processor);

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
    void unregister(String contentType);

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
     * @param resource    the resource to index
     * @param context     the context to which validation errors and warnings are reported
     * @throws InstallException if there was a problem indexing the contribution
     */
    void indexResource(Resource resource, IntrospectionContext context) throws InstallException;

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
