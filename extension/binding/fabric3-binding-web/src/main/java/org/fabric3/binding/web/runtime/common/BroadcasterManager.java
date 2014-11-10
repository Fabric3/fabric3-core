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
package org.fabric3.binding.web.runtime.common;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;

/**
 * Manages active Atmosphere <code>Broadcaster</code> instances.
 */
public interface BroadcasterManager {

    /**
     * Returns the Broadcaster for the HTTP URL path, creating one if necessary.
     *
     * @param path   the path
     * @param config the Atmosphere configuration
     * @return the Broadcaster
     */
    Broadcaster getChannelBroadcaster(String path, AtmosphereConfig config);

    /**
     * Returns the Broadcaster for the HTTP URL path, creating one if necessary.
     *
     * @param path   the path
     * @param config the Atmosphere configuration
     * @return the Broadcaster
     */
    Broadcaster getServiceBroadcaster(String path, AtmosphereConfig config);

    /**
     * Disposes a Broadcaster for the HTTP URL path.
     *
     * @param path the path
     */
    void remove(String path);
}
