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
 */
package org.fabric3.spi.domain;

import org.fabric3.spi.domain.generator.Deployment;

/**
 * Encapsulates a Deployment. The current Deployment contains the incremental DeploymentUnits which are to be applied to zones in the domain. The full
 * Deployment contains the complete DeploymentUnits necessary to update a participant runtime to the current state of its zone. The full
 * DeploymentUnits are cached by zone peers for fault-tolerance: a participant runtime may contact a peer when it boots to update to the current zone
 * state without the need to contact the controller.
 */
public class DeploymentPackage {
    private Deployment currentDeployment;
    private Deployment fullDeployment;

    /**
     * Constructor.
     *
     * @param currentDeployment the current incremental deployment
     * @param fullDeployment    the full deployment
     */
    public DeploymentPackage(Deployment currentDeployment, Deployment fullDeployment) {
        this.currentDeployment = currentDeployment;
        this.fullDeployment = fullDeployment;
    }

    /**
     * Returns the current incremental deployment.
     *
     * @return the current incremental deployment.
     */
    public Deployment getCurrentDeployment() {
        return currentDeployment;
    }

    /**
     * Returns the full deployment.
     *
     * @return the full deployment
     */
    public Deployment getFullDeployment() {
        return fullDeployment;
    }
}
