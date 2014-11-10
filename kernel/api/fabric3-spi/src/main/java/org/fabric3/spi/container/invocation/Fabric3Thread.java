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
 * A managed thread in a Fabric3 runtime that implements its own thread pooling. Not present in hosted environments that have external thread management.
 */
public class Fabric3Thread extends Thread {
    private Message message;
    private WorkContext workContext;

    public Fabric3Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    /**
     * Returns the message associated with the thread or null if one has not be assigned.
     *
     * @return the message associated with the thread or null
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Associates a message with the thread.
     *
     * @param message the message
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Returns the work context associated with the thread or null if one has not be assigned.
     *
     * @return the work context associated with the thread or null
     */
    public WorkContext getWorkContext() {
        return workContext;
    }

    /**
     * Associates a work context with the thread.
     *
     * @param workContext the work context
     */
    public void setWorkContext(WorkContext workContext) {
        this.workContext = workContext;
    }
}
