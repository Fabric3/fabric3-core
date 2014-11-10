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
package org.fabric3.binding.rs.runtime.container;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class RsContainerManagerImpl implements RsContainerManager {
    private Map<URI, RsContainer> containers = new ConcurrentHashMap<>();

    public void register(URI name, RsContainer container) {
        containers.put(name, container);
    }

    public void unregister(URI name) {
        containers.remove(name);
    }

    public RsContainer get(URI name) {
        return containers.get(name);
    }
}
