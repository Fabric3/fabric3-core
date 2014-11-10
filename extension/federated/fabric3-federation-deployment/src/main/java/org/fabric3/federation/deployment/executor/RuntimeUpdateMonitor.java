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

/**
 *
 */
public interface RuntimeUpdateMonitor {

    /**
     * Callback when an update request is received from a runtime.
     *
     * @param id the runtime id.
     */
    @Debug("Update request received from {0}")
    void updateRequest(String id);

    @Debug("Sending update to {0}")
    void sendingUpdate(String id);

    @Debug("Runtime is not updated. Unable to service request from {0}")
    void notUpdated(String id);

}