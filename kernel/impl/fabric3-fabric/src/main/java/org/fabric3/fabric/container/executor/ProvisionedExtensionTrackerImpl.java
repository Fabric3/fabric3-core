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
package org.fabric3.fabric.container.executor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ProvisionedExtensionTrackerImpl implements ProvisionedExtensionTracker {
    private Map<URI, Integer> cache = new HashMap<>();

    public synchronized void increment(URI uri) {
        Integer count = cache.get(uri);
        if (count == null) {
            cache.put(uri, 1);
        } else {
            cache.put(uri, count + 1);
        }

    }

    public synchronized int decrement(URI uri) {
        Integer count = cache.get(uri);
        if (count == null) {
            return -1;
        } else if (count == 1) {
            cache.remove(uri);
            return 0;
        } else {
            count = count - 1;
            cache.put(uri, count);
            return count;
        }
    }
}
