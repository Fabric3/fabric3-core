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
package org.fabric3.api.host.domain;

import java.net.URI;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Composite;

/**
 * A domain.
 */
public interface Domain {

    /**
     * Include all deployables contained in the list of contributions in the domain. This operation is intended for composites that are synthesized from
     * multiple deployable composites that are associated with individual deployment plans.
     *
     * @param uris the contributions to deploy
     * @throws Fabric3Exception if an error is encountered during inclusion
     */
    void include(List<URI> uris) throws Fabric3Exception;

    /**
     * Include a composite in the domain.
     *
     * @param composite the composite to include
     * @throws Fabric3Exception if an error is encountered during inclusion
     */
    void include(Composite composite) throws Fabric3Exception;

    /**
     * Remove all deployables in a contribution from the domain.
     *
     * @param uri   the contribution URI
     * @throws Fabric3Exception if an error is encountered during undeployment
     */
    void undeploy(URI uri) throws Fabric3Exception;

    /**
     * Undeploys a composite.
     *
     * @param composite the composite
     * @throws Fabric3Exception if an error is encountered during undeployment
     */
    void undeploy(Composite composite) throws Fabric3Exception;


}
