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
import org.fabric3.spi.container.command.ResponseCommand;

/**
 * Broadcast by the controller to to perform a deployment to all participants in a zone. The current deployment is incremental from the previous
 * deployment. The full deployment contains the complete list of commands required to update a participant runtime to the current zone state. The
 * latter is cached by participants which can be used to bootstrap zone peers without the need to contact the controller.
 */
public class DeploymentCommand implements ResponseCommand, Response {
    private static final long serialVersionUID = 8673100303949676875L;

    private String zone;
    private SerializedDeploymentUnit currentDeploymentUnit;
    private SerializedDeploymentUnit fullDeploymentUnit;
    private Response response;
    private String runtimeName;

    public DeploymentCommand(String zone, SerializedDeploymentUnit currentDeploymentUnit, SerializedDeploymentUnit fullDeploymentUnit) {
        this.zone = zone;
        this.currentDeploymentUnit = currentDeploymentUnit;
        this.fullDeploymentUnit = fullDeploymentUnit;
    }

    public String getZone() {
        return zone;
    }

    public SerializedDeploymentUnit getCurrentDeploymentUnit() {
        return currentDeploymentUnit;
    }

    public SerializedDeploymentUnit getFullDeploymentUnit() {
        return fullDeploymentUnit;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }
}
