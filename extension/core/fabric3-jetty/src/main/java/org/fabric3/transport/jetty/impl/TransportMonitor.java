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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transport.jetty.impl;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.api.annotation.monitor.Warning;

/**
 * The monitoring interfaces used by the Jetty system service
 */
public interface TransportMonitor {

    @Info("HTTP listener started on port {0,number,#}")
    void startHttpListener(int port);

    @Info("HTTPS listener started on port {0,number,#}")
    void startHttpsListener(int port);

    /**
     * Captures Jetty warnings.
     *
     * @param msg  the warning message
     * @param args arguments
     */
    @Warning("Jetty warning: {0} \n {1}")
    void warn(String msg, Object... args);

    /**
     * Captures Jetty exceptions
     *
     * @param msg  the warning message
     * @param args the exceptions
     */
    @Severe("Jetty exception: {0}")
    void exception(String msg, Throwable args);

    /**
     * Captures Jetty debug events.
     *
     * @param msg  the debug message
     * @param args arguments
     */
    @Debug("Jetty debug: {0} \n {1}")
    void debug(String msg, Object... args);

}
