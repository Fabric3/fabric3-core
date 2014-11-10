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
package org.fabric3.spi.federation.topology;

/**
 * Receives callbacks when a domain topology changes.
 */
public interface TopologyListener {

    /**
     * Callback when a runtime joins a domain.
     *
     * @param name the runtime name
     */
    void onJoin(String name);

    /**
     * Callback when a runtime leaves a domain.
     *
     * @param name the runtime name
     */
    void onLeave(String name);

    /**
     * Callback when a runtime is elected leader in a domain.
     *
     * @param name the runtime name
     */
    void onLeaderElected(String name);

}
