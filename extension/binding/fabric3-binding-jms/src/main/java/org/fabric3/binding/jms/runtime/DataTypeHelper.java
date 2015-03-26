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
package org.fabric3.binding.jms.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class DataTypeHelper {

    public static final DataType JAXB_TYPE = new JavaType(String.class, "JAXB");
    public static List<DataType> JAXB_TYPES = Arrays.asList(JAXB_TYPE);

    public static List<DataType> createTypes(PhysicalOperation physicalOperation) throws Fabric3Exception {
        List<DataType> dataTypes = new ArrayList<>();
        if (!physicalOperation.getSourceParameterTypes().isEmpty()) {
            List<Class<?>> types = physicalOperation.getSourceParameterTypes();
            dataTypes.addAll(types.stream().map(type -> new JavaType((type))).collect(Collectors.toList()));
        }
        return dataTypes;
    }

    private DataTypeHelper() {
    }
}
