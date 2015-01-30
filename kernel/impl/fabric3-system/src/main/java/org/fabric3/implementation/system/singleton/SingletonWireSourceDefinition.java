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
package org.fabric3.implementation.system.singleton;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 *
 */
public class SingletonWireSourceDefinition extends PhysicalWireSourceDefinition {
    private static final long serialVersionUID = -8954551053778417834L;
    private Injectable injectable;

    /**
     * Returns the Injectable for this wire. This identifies which reference or resource on the component this wire applies to.
     *
     * @return the value source for this wire
     */
    public Injectable getInjectable() {
        return injectable;
    }

    /**
     * Sets the Injectable associated with this wire.
     *
     * @param injectable the Injectable source associated with this wire
     */
    public void setInjectable(Injectable injectable) {
        this.injectable = injectable;
    }


}