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
package org.fabric3.binding.file.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Generated metadata used for attaching a reference to a ZeroMQ Socket.
 */
public class FileBindingWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 1182695578174617840L;

    private String location;
    private String adapterClass;
    private URI adapterUri;

    public FileBindingWireTargetDefinition(String location, String adapterClass, URI adapterUri) {
        this.location = location;
        this.adapterClass = adapterClass;
        this.adapterUri = adapterUri;
        setUri(null);
    }

    /**
     * The directory to receive files in. May be relative or absolute. If it is relative, it will be resolved against the runtime data directory.
     *
     * @return the directory to receive files in
     */
    public String getLocation() {
        return location;
    }


    /**
     * The adapter class for processing received files.
     *
     * @return the adapter class for processing received files or null
     */
    public String getAdapterClass() {
        return adapterClass;
    }

    /**
     * Returns the URI of the adaptor component for receiving files.
     *
     * @return the URI of the adaptor component for receiving files or null
     */
    public URI getAdapterUri() {
        return adapterUri;
    }

}
