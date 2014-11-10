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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.container.invocation;

/**
 * Represents a request, response, or exception flowing through a wire.
 */
public interface Message {

    /**
     * Returns the body of the message, which will be the payload or parameters associated with the wire.
     *
     * @return the body of the message
     */
    Object getBody();

    /**
     * Sets the body of the message.
     *
     * @param body the body of the message
     */
    void setBody(Object body);

    /**
     * Set the message body with a fault object. After this method is called, isFault() returns true.
     *
     * @param fault The fault object represents an exception
     */
    void setBodyWithFault(Object fault);

    /**
     * Determines if the message represents a fault/exception
     *
     * @return true if the message body is a fault object, false if the body is a normal payload
     */
    boolean isFault();

    /**
     * Returns the context associated with this invocation.
     *
     * @return the context associated with this invocation
     */
    WorkContext getWorkContext();

    /**
     * Sets the context associated with this invocation.
     *
     * @param workContext the context associated with this invocation
     */
    void setWorkContext(WorkContext workContext);

    /**
     * Resets the message making it available for reuse.
     */
    void reset();
}
