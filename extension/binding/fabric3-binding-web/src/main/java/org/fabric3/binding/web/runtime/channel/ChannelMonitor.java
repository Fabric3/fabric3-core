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
package org.fabric3.binding.web.runtime.channel;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 *
 */
public interface ChannelMonitor {

    @Info("HTTP/Websocket channel endpoint provisioned at {0}")
    void provisionedChannelEndpoint(String path);

    @Info("HTTP/Websocket channel endpoint removed at {0}")
    void removedChannelEndpoint(String path);

    @Severe("HTTP/websocket error encountered: {0}")
    void error(Throwable t);

    @Debug("Atmosphere channel event {0}")
	void eventing(String string);
    
    @Debug("HTTP/Websocket channel event {0}")
	void eventingWS(String string);
    
}
