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
package org.fabric3.spi.contribution.archive;

import java.util.function.Consumer;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Processes a contribution manifest and iterates contained artifacts in an archive contribution type during introspection.
 */
public interface ArchiveContributionHandler {

    /**
     * Returns true if the implementation can process the contribution archive.
     *
     * @param contribution the contribution
     * @return true if the implementation can process the contribution archive
     */
    boolean canProcess(Contribution contribution);

    /**
     * Processes the manifest
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws Fabric3Exception if an error occurs processing the manifest
     */
    void processManifest(Contribution contribution, IntrospectionContext context) throws Fabric3Exception;

    /**
     * Iterates through a contribution calling the supplied action when a contained artifact is encountered.
     *
     * @param contribution the contribution
     * @param callback     the action to perform when an artifact is encountered
     * @param context      the context to which validation errors and warnings are reported
     * @throws Fabric3Exception if an error occurs processing the manifest
     */
    void iterateArtifacts(Contribution contribution, Consumer<Resource> callback, IntrospectionContext context) throws Fabric3Exception;

}
