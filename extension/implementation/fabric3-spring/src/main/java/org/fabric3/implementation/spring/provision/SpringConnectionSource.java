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
package org.fabric3.implementation.spring.provision;

import java.net.URI;

import org.fabric3.implementation.pojo.provision.PojoConnectionSource;

/**
 * Used to attach the source side of a channel connection to a Spring component producer.
 */
public class SpringConnectionSource extends PojoConnectionSource {
    private String producerName;

    /**
     * Constructor.
     *
     * @param producerName  the producer name.
     * @param interfaze the producer interface name
     * @param uri           the source Spring component URI;
     */
    public SpringConnectionSource(String producerName, Class<?> interfaze, URI uri) {
        this.producerName = producerName;
        setServiceInterface(interfaze);
        setUri(uri);
    }

    /**
     * Returns the producer name.
     *
     * @return the producer name
     */
    public String getProducerName() {
        return producerName;
    }

}