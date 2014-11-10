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
package org.fabric3.spi.host;

import java.io.Serializable;

/**
 * A reserved port on a runtime. After reserving a port, clients must release the port lock prior to binding a socket to the port using {@link
 * #bind(Port.TYPE)}.
 */
public interface Port extends Serializable {

    public enum TYPE {
        TCP, UDP
    }

    /**
     * Returns the port name.
     *
     * @return the port name
     */
    String getName();

    /**
     * Returns the port number.
     *
     * @return the port number
     */
    int getNumber();

    /**
     * Prepares the port so that a socket may be bound. This method may be called any number of times.
     *
     * @param type the socket type to bind
     */
    void bind(TYPE type);

    /**
     * Releases all port locks. This method may be called any number of times.
     */
    void release();

}
