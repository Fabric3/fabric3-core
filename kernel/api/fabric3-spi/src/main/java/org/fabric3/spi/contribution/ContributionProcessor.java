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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Interface for services that process contributions. Contribution processing occurs in several phases. Contribution metadata is first processed, after which
 * contained resources are indexed. Indexed {@link Resource}s contain 0..n {@link ResourceElement}s, which are addressable parts. ResourceElements contain a key
 * for a symbol space and a value. When a resource is indexed, only ResourceElement keys are available; their values have not yet been loaded.  The final
 * processing phase is when the contribution is loaded. At this point, all contribution artifacts, including those in dependent contributions, are made
 * available through the provided classloader. Indexed Resources are iterated and all ResourceElement values are loaded via the loader framework. As
 * ResourceElements may refer to other ResourceElements, loading may occur recursively.
 */
public interface ContributionProcessor {

    /**
     * Returns true if the processor can process the contribution.
     *
     * @param contribution the contribution
     * @return true if the processor can process the contribution.
     */
    boolean canProcess(Contribution contribution);

    /**
     * Processes manifest information for the contribution, including imports and exports.
     *
     * @param contribution the contribution that will be used to hold the results from the processing
     * @param context      the context to which validation errors and warnings are reported
     * @throws Fabric3Exception if there was a problem with the contribution
     */
    void processManifest(Contribution contribution, IntrospectionContext context) throws Fabric3Exception;

    /**
     * Indexes all contribution resources
     *
     * @param contribution the contribution to index
     * @param context      the context to which validation errors and warnings are reported
     * @throws Fabric3Exception if there was a problem indexing
     */
    void index(Contribution contribution, IntrospectionContext context) throws Fabric3Exception;

    /**
     * Loads all resources in the contribution.
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws Fabric3Exception if there was a problem loading the contribution resources
     */
    void process(Contribution contribution, IntrospectionContext context) throws Fabric3Exception;

}
