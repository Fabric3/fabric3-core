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
package org.fabric3.federation.jgroups;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 *
 */
public interface TopologyServiceMonitor {

    @Severe
    void error(String message, Throwable t);

    @Info("Joined domain as: {0}")
    void joinedDomain(String name);

    @Info("Disconnected from domain")
    void disconnect();

    @Debug("No runtimes found in the domain")
    void noRuntimes();

    @Debug("Received domain snapshot from {0}")
    void receivedSnapshot(String name);

    @Debug("Handling message from: {0}")
    void handleMessage(String name);

    @Debug("Received message from: {0}")
    void receiveMessage(String name);

    @Debug("Update request sent to: {0}")
    void updating(String s);

    @Debug("Completed update")
    void updated();

    @Debug("Unable to update the runtime until a controller becomes available")
    void updateDeferred();

    @Debug("Runtime joined the domain: {0}")
    void runtimeJoined(String name);

    @Debug("Runtime removed from the domain: {0}")
    void runtimeRemoved(String name);

    @Debug("Broadcasting availability to the domain")
    void broadcastAvailability();

}
