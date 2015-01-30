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
package org.fabric3.api.binding.zeromq.model;

import java.net.URI;

import org.fabric3.api.model.type.component.BindingDefinition;

/**
 * A ZeroMQ binding configuration set on a channel, reference, or composite.
 */
public class ZeroMQBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 4154636613386389578L;

    private ZeroMQMetadata metadata;
    private URI targetUri;

    public ZeroMQBindingDefinition(String bindingName, ZeroMQMetadata metadata) {
        this(bindingName, null, metadata);
    }

    public ZeroMQBindingDefinition(String bindingName, URI targetUri, ZeroMQMetadata metadata) {
        super(bindingName, targetUri, "zeromq");
        this.metadata = metadata;
    }

    public ZeroMQMetadata getZeroMQMetadata() {
        return metadata;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }
}
