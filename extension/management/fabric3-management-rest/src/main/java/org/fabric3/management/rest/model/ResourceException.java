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
package org.fabric3.management.rest.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Raised by framework services when an exception is encountered processing a resource request that must be returned to the client.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class ResourceException extends Fabric3Exception {
    private static final long serialVersionUID = 228120523405433691L;

    private HttpStatus status;
    private Map<String, String> headers;
    private Object entity;

    /**
     * Constructor.
     *
     * @param status the HTTP status that should be sent to the client
     */
    public ResourceException(HttpStatus status) {
        this.status = status;
    }

    /**
     * Constructor.
     *
     * @param status  the HTTP status that should be sent to the client
     * @param message a message that should be returned to the client
     */
    public ResourceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Constructor.
     *
     * @param status  the HTTP status that should be sent to the client
     * @param message a message that should be returned to the client
     * @param t       the root exception
     */
    public ResourceException(HttpStatus status, String message, Throwable t) {
        super(message, t);
        this.status = status;
    }

    /**
     * Returns the HTTP status that should be sent to the client.
     *
     * @return the HTTP status that should be sent to the client
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Returns the HTTP headers that should be sent to the client.
     *
     * @return the HTTP headers that should be sent to the client
     */
    public Map<String, String> getHeaders() {
        if (headers == null) {
            return Collections.emptyMap();
        }
        return headers;
    }

    /**
     * Adds an HTTP header.
     *
     * @param name  the header name
     * @param value the header value
     */
    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
