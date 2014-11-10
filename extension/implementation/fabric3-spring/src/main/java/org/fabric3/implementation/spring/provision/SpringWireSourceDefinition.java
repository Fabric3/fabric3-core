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
package org.fabric3.implementation.spring.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Metadata for attaching a wire to a reference in a Spring application context.
 */
public class SpringWireSourceDefinition extends PhysicalWireSourceDefinition {
    private static final long serialVersionUID = 5648037666523575314L;
    private String referenceName;
    private String interfaze;

    /**
     * Constructor.
     *
     * @param referenceName the reference name.
     * @param interfaze     the reference interface as a fully qualified Java class name
     * @param uri           the source Spring component URI;
     */
    public SpringWireSourceDefinition(String referenceName, String interfaze, URI uri) {
        this.referenceName = referenceName;
        this.interfaze = interfaze;
        setUri(uri);
    }

    /**
     * Returns the reference name.
     *
     * @return the reference name
     */
    public String getReferenceName() {
        return referenceName;
    }

    /**
     * Returns the reference interface as a fully qualified Java class name.
     *
     * @return the reference interface
     */
    public String getInterface() {
        return interfaze;
    }
}
