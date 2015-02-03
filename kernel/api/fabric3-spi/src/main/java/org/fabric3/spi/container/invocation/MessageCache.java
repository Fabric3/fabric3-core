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
package org.fabric3.spi.container.invocation;

/**
 * Cache of {@link Message}s associated with runtime threads.
 *
 * On runtimes with managed thread pools, the cache uses {@link Fabric3Thread} to store the message; on other runtimes a thread local is used.
 */
public class MessageCache {
    private static final ThreadLocal<Message> CONTEXT = new ThreadLocal<>();

    private MessageCache() {
    }

    /**
     * Returns the Message for the current thread.
     *
     * @return the Message for the current thread
     */
    public static Message getMessage() {
        Thread thread = Thread.currentThread();
        if (thread instanceof Fabric3Thread) {
            Fabric3Thread fabric3Thread = (Fabric3Thread) thread;
            Message message = fabric3Thread.getMessage();
            if (message == null) {
                message = new MessageImpl();
                fabric3Thread.setMessage(message);
            }
            return message;
        } else {
            Message message = CONTEXT.get();
            if (message == null) {
                message = new MessageImpl();
                CONTEXT.set(message);
            }
            return message;
        }
    }

    /**
     * Resets and returns the Message for the current thread.
     *
     * @return the Message for the current thread
     */
    public static Message getAndResetMessage() {
        Message message = getMessage();
        message.reset();
        return message;
    }
}
