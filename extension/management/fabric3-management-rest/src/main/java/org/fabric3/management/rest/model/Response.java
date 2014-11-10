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

/**
 * A response returned by a resource framework service.
 */
public class Response {
    private HttpStatus status;
    private Map<String, String> headers;
    private Object entity;

    /**
     * Constructor.
     *
     * @param status the HTTP status that should be sent to the client
     */
    public Response(HttpStatus status) {
        this.status = status;
    }

    /**
     * Constructor.
     *
     * @param status the HTTP status that should be sent to the client
     * @param entity the resource representation that should be returned to the client
     */
    public Response(HttpStatus status, Object entity) {
        this.status = status;
        this.entity = entity;
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
     * Returns the resource representation that should be returned to the client/
     *
     * @return the resource representation that should be returned to the client
     */
    public Object getEntity() {
        return entity;
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
}
