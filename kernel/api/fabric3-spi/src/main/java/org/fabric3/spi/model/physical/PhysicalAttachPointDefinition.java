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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Metadata for attaching a wire or channel connection to a source or target.
 */
public abstract class PhysicalAttachPointDefinition implements Serializable {
    private static final long serialVersionUID = -1905533250691356716L;

    private URI uri;
    private ClassLoader classLoader;
    protected List<DataType> dataTypes = new ArrayList<>();

    public PhysicalAttachPointDefinition() {
        // default to Java
        dataTypes.add(PhysicalDataTypes.JAVA_TYPE);
    }

    public PhysicalAttachPointDefinition(DataType... types) {
        if (types != null) {
            dataTypes.addAll(Arrays.asList(types));
        }
    }

    /**
     * Returns the URI of the attach point such as a reference, callback, resource, producer, service or consumer.
     *
     * @return the attach point URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI of the attach point such as a reference, callback, resource, producer, service or consumer.
     *
     * @param uri the attach point URI
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the classloader associated with the attach point.
     *
     * @return the classloader associated with the attach point
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the id of the classloader associated with the attach point.
     *
     * @param classLoader the id of the classloader associated with the attach point
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns a list of supported data types supported by the attach point by order of preference.
     *
     * @return a list of supported data types by order of preference
     */
    public List<DataType> getDataTypes() {
        return dataTypes;
    }

}
