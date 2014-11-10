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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.file.runtime.receiver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ReceiverManagerImpl implements ReceiverManager {
    private Map<String, FileSystemReceiver> receivers = new ConcurrentHashMap<>();

    public void create(ReceiverConfiguration configuration) {
        String id = configuration.getId();
        if (receivers.containsKey(id)) {
            throw new AssertionError("Duplicate receiver: " + id);
        }
        FileSystemReceiver receiver = new FileSystemReceiver(configuration);
        receiver.start();
        receivers.put(id, receiver);
    }

    public void remove(String id) {
        FileSystemReceiver receiver = receivers.remove(id);
        if (receiver != null) {
            receiver.stop();
        }
    }

}
