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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.SecuritySubject;

/**
 * Tracks information associated with a request as it is processed by the runtime. Requests originate at a domain boundary (e.g. a service bound to a
 * transport).
 *
 * WorkContext instances are cached per runtime thread and reused. This implementation is <em>not</em> thread safe.
 */
public class WorkContext implements Serializable {
    private static final long serialVersionUID = 9108092492339191639L;
    private transient SecuritySubject subject;
    private List<String> callStack;
    private transient Map<String, Object> headers;

    public void setSubject(SecuritySubject subject) {
        this.subject = subject;
    }

    /**
     * Gets the subject associated with the current invocation.
     *
     * @return Subject associated with the current invocation.
     */
    public SecuritySubject getSubject() {
        return subject;
    }

    /**
     * Adds a callback reference to the work context.
     *
     * @param callbackReference the callback reference to add
     */
    public void addCallbackReference(String callbackReference) {
        if (callStack == null) {
            callStack = new ArrayList<>();
        }
        callStack.add(callbackReference);
    }

    /**
     * Adds a collection of callback references to the work context.
     *
     * @param callbackReferences the collection of callback references to add
     */
    public void addCallbackReferences(List<String> callbackReferences) {
        if (callStack == null) {
            callStack = callbackReferences;
            return;
        }
        callStack.addAll(callbackReferences);
    }

    /**
     * Removes and returns the callback reference associated with the previous request from the internal stack.
     *
     * @return the callback reference
     */
    public String popCallbackReference() {
        if (callStack == null || callStack.isEmpty()) {
            return null;
        }
        return callStack.remove(callStack.size() - 1);
    }

    /**
     * Returns but does not remove the callback reference associated with the previous request from the internal stack.
     *
     * @return the callback reference.
     */
    public String peekCallbackReference() {
        if (callStack == null || callStack.isEmpty()) {
            return null;
        }
        return callStack.get(callStack.size() - 1);
    }

    /**
     * Returns the callback reference stack.
     *
     * @return the stack
     */
    public List<String> getCallbackReferences() {
        return callStack;
    }

    /**
     * Returns the header value for the given name associated with the current request context.
     *
     * @param type the expected header value type
     * @param name the header name
     * @return the header value or null if no header exists
     */
    public <T> T getHeader(Class<T> type, String name) {
        if (headers == null) {
            return null;
        }
        return type.cast(headers.get(name));
    }

    /**
     * Sets a header value for the current request context.
     *
     * @param name  the header name
     * @param value the header vale
     */
    public void setHeader(String name, Object value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    /**
     * Clears a header for the current request context.
     *
     * @param name the header name
     */
    public void removeHeader(String name) {
        if (headers == null) {
            return;
        }
        headers.remove(name);
    }

    /**
     * Returns all headers for the current request context.
     *
     * @return the map of header names and values
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * Replaces the existing headers with the new headers.
     *
     * @param newHeaders the new headers
     */
    public void addHeaders(Map<String, Object> newHeaders) {
        if (headers == null) {
            headers = newHeaders;
            return;
        }
        headers.putAll(newHeaders);
    }

    /**
     * Resets the work context so that it may be reused for another request.
     */
    public void reset() {
        subject = null;
        headers = null;
        callStack = null;
    }
}
