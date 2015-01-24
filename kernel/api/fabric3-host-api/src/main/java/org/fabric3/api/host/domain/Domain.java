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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.api.model.type.component.Composite;

/**
 * Represents a domain.
 */
public interface Domain {

    /**
     * Include a deployable composite in the domain.
     *
     * @param deployable the name of the deployable composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable) throws DeploymentException;

    /**
     * Include all deployables contained in the list of contributions in the domain. This operation is intended for composites that are synthesized from
     * multiple deployable composites that are associated with individual deployment plans.
     *
     * @param uris the contributions to deploy
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(List<URI> uris) throws DeploymentException;

    /**
     * Include a composite in the domain.
     *
     * @param composite the composite to include
     * @param simulated true if the include is a simulation. Simulated includes skip generation and deployment to runtimes. In addition, simulated deployments
     *                  are not fail-fast, i.e. they will be completed if assembly errors exist.
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(Composite composite, boolean simulated) throws DeploymentException;

    /**
     * Remove all deployables in a contribution from the domain.
     *
     * @param uri   the contribution URI
     * @param force true if the undeployment operation should ignore errors from runtimes and remove logical components on the controller. If true, undeployment
     *              will also succeed if no participants are available.
     * @throws DeploymentException if an error is encountered during undeployment
     */
    void undeploy(URI uri, boolean force) throws DeploymentException;

    /**
     * Undeploys the composite.
     *
     * @param composite the composite
     * @param simulated true if the include is a simulation. Simulated includes skip generation and deployment to runtimes.
     * @throws DeploymentException if an error is encountered during undeployment
     */
    void undeploy(Composite composite, boolean simulated) throws DeploymentException;

    /**
     * Activates a set of definitions contained in the contribution.
     *
     * @param uri the contribution URI
     * @throws DeploymentException if an error is encountered during activation
     */
    void activateDefinitions(URI uri) throws DeploymentException;

    /**
     * Deactivates a set of definitions contained in the contribution.
     *
     * @param uri the contribution URI
     * @throws DeploymentException if an error is encountered during activation
     */
    void deactivateDefinitions(URI uri) throws DeploymentException;

}
