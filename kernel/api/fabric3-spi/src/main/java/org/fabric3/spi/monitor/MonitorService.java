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
package org.fabric3.spi.monitor;

import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 * Manages and exposes configuration for the runtime monitor implementation.
 */
public interface MonitorService {

    /**
     * Sets the monitoring level for components under the given hierarchical URI.
     *
     * @param uri   the component URI
     * @param level the monitoring level to set
     */
    void setComponentLevel(String uri, String level);

    /**
     * Sets the monitoring level on the library provider.
     *
     * @param key   the provider key
     * @param level the level
     */
    void setProviderLevel(String key, String level);

    /**
     * Returns the monitoring level for the library provider.
     *
     * @param key the provider key
     * @return the level
     */
    MonitorLevel getProviderLevel(String key);

}
