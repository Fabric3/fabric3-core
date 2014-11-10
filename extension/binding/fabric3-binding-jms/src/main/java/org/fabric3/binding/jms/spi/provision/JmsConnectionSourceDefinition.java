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
package org.fabric3.binding.jms.spi.provision;

import java.net.URI;

import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;

/**
 * Generated metadata used for attaching channels and consumers to a JMS destination.
 */
public class JmsConnectionSourceDefinition extends PhysicalConnectionSourceDefinition {
    private static final long serialVersionUID = -1573426921591142923L;
    private JmsBindingMetadata metadata;
    private SessionType sessionType;

    /**
     * Constructor.
     *
     * @param uri         the service URI
     * @param metadata    metadata used to create a JMS message consumer
     * @param type        the data type events should be deserialized from
     * @param sessionType the session type
     */
    public JmsConnectionSourceDefinition(URI uri, JmsBindingMetadata metadata, DataType type, SessionType sessionType) {
        super(type);
        this.metadata = metadata;
        this.sessionType = sessionType;
        setUri(uri);
    }

    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

    public SessionType getSessionType() {
        return sessionType;
    }
}