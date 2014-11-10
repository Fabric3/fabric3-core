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
package org.fabric3.federation.deployment.executor;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Severe;

/**
 *
 */
public interface DeploymentCommandExecutorMonitor {

    /**
     * Callback when a deployment is received.
     */
    @Debug("Deployment received")
    void received();

    /**
     * Callback when a deployment is completed.
     */
    @Debug("Completed deployment")
    void completed();

    /**
     * Callback when an error is thrown processing a deployment.
     *
     * @param e the error
     */
    @Severe("Deployment error")
    void error(Throwable e);

    /**
     * Callback when an error is thrown processing a deployment.
     *
     * @param msg the error message
     * @param e   the error
     */
    @Severe
    void errorMessage(String msg, Throwable e);

}
