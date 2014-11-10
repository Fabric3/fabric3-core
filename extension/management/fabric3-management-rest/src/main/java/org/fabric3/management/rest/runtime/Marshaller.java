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
package org.fabric3.management.rest.runtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;

/**
 * Serializes and deserializes request and response values.
 */
public interface Marshaller {

    /**
     * Deserializes request parameters from either the request URL or request contents.
     *
     * @param verb    the HTTP verb
     * @param request the request
     * @param mapping the associated resource mapping
     * @return the deserialized parameters
     * @throws ResourceException if a deserialization error occurs
     */
    Object[] deserialize(Verb verb, HttpServletRequest request, ResourceMapping mapping) throws ResourceException;

    /**
     * Serializes a response value.
     *
     * @param value    the response value
     * @param mapping  the resource mapping
     * @param request  the current request
     * @param response the current response
     * @throws ResourceException if an error handling the request occurs
     */
    void serialize(Object value, ResourceMapping mapping, HttpServletRequest request, HttpServletResponse response) throws ResourceException;

}
