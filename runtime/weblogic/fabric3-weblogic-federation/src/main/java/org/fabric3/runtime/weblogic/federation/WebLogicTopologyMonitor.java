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
package org.fabric3.runtime.weblogic.federation;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 * Receives federation callback events.
 */
public interface WebLogicTopologyMonitor {

    @Severe("An error was encountered")
    void error(Throwable error);

    @Severe
    void errorMessage(String message, Throwable error);

    @Severe
    void errorMessage(String message);

    @Info("Attempting to connect to admin server")
    void connectingToAdminServer();

    @Info("Admin server is unavailable")
    void adminServerUnavailable();

    @Info("Update request sent to admin server")
    void updating();

    @Info("Update received from admin server")
    void updated();

    @Info("No managed servers found")
    void noManagedServers();

    @Debug
    void errorDetail(Exception e);
}
