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

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * Defines well-known physical data types. Binding and component extensions may declare the data types they support for service operation parameters on {@link
 * PhysicalWireTarget}.
 */
public interface PhysicalDataTypes {

    /**
     * Denotes a Java type, i.e. an object or primitive
     */
    DataType JAVA_TYPE = new JavaType(Object.class);

    /**
     * Denotes a JAXB type
     */
    DataType JAXB = new JavaType(String.class, "JAXB");


}
