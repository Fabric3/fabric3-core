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

import java.io.Serializable;

import org.fabric3.management.rest.spi.Verb;

/**
 * Used to replicate resource requests to participants in a zone.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class ReplicationEnvelope implements Serializable {
    private static final long serialVersionUID = -7548186506338136783L;
    private String path;
    private Verb verb;
    private Object[] params;

    /**
     * Constructor.
     *
     * @param path   the request path
     * @param verb   the HTTP request verb
     * @param params the request params
     */
    public ReplicationEnvelope(String path, Verb verb, Object[] params) {
        this.path = path;
        this.verb = verb;
        this.params = params;
    }

    public String getPath() {
        return path;
    }

    public Verb getVerb() {
        return verb;
    }

    public Object[] getParams() {
        return params;
    }
}
