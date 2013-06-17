/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
            headers = new HashMap<String, String>();
        }
        headers.put(name, value);
    }
}
