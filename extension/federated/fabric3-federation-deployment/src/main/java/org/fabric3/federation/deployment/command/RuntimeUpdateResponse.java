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
package org.fabric3.federation.deployment.command;

import org.fabric3.spi.container.command.Response;

/**
 * A response to a {@link RuntimeUpdateCommand}. The response may contain a {@link DeploymentCommand} to be executed by the originating runtime or may
 * indicate the target runtime is not updated with a current deployment (in which case the deployment command will be null). A controller will always
 * return a deployment command, while a peer runtime may return a not updated status.
 */
public class RuntimeUpdateResponse implements Response {
    private static final long serialVersionUID = -4131501898263082374L;
    private String runtimeName;
    private DeploymentCommand deploymentCommand;
    private boolean updated;

    public RuntimeUpdateResponse() {
    }

    public RuntimeUpdateResponse(DeploymentCommand deploymentCommand) {
        this.deploymentCommand = deploymentCommand;
        updated = true;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String name) {
        runtimeName = name;
    }

    public boolean isUpdated() {
        return updated;
    }

    public DeploymentCommand getDeploymentCommand() {
        return deploymentCommand;
    }

}