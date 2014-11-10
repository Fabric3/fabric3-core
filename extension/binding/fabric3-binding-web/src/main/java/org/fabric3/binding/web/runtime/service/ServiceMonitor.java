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
package org.fabric3.binding.web.runtime.service;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 *
 */
public interface ServiceMonitor {

    @Info("HTTP/Websocket endpoint provisioned at {0}")
    void provisionedEndpoint(String path);

    @Info("Channel HTTP/Websocket endpoint removed at {0}")
    void removedEndpoint(String path);
    
    @Debug("Event {0}")
    void eventing(String event);

    @Severe("HTTP/websocket error encountered: {0}")
    void error(Throwable e);
}
