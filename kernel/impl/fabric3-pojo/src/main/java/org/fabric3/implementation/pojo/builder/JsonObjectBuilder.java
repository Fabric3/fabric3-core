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

package org.fabric3.implementation.pojo.builder;

import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;

/**
 * Builds a Supplier that creates a property value object from a JSON string. The serialized value must be either a String or in JSON form.
 */
public interface JsonObjectBuilder {

    /**
     * Creates the Supplier for the property value.
     *
     * @param name  the property name
     * @param value the serialized property value
     * @param type  the property type
     * @return the Supplier
     * @throws Fabric3Exception if there is an error creating the Supplier
     */
    Supplier<?> createSupplier(String name, String value, DataType type, ClassLoader classLoader) throws Fabric3Exception;

}