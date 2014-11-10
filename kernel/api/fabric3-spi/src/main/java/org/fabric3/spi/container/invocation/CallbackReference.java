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
package org.fabric3.spi.container.invocation;

import java.io.Serializable;

/**
 * A callback endpoint reference.
 */
public class CallbackReference implements Serializable {
    private static final long serialVersionUID = -6108279393891496098L;

    private String uri;
    private String correlationId;

    /**
     * Constructor.
     *
     * @param uri           the callback URI
     * @param correlationId the correlation id. For stateless targets, the id may be null.
     */
    public CallbackReference(String uri, String correlationId) {
        this.uri = uri;
        this.correlationId = correlationId;
    }

    /**
     * Returns the callback URI.
     *
     * @return the callback URI
     */
    public String getServiceUri() {
        return uri;
    }

    /**
     * Returns the correlation id or null if the target is stateless.
     *
     * @return the correlation id or null.
     */
    public String getCorrelationId() {
        return correlationId;
    }

}
