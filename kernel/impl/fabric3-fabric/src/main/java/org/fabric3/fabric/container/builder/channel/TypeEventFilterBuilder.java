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
package org.fabric3.fabric.container.builder.channel;

import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.fabric.model.physical.TypePhysicalEventFilter;
import org.fabric3.spi.container.builder.channel.EventFilter;
import org.fabric3.spi.container.builder.channel.EventFilterBuilder;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * Creates an event filter that filters based on Java types.
 */
public class TypeEventFilterBuilder implements EventFilterBuilder<TypePhysicalEventFilter> {

    public EventFilter build(TypePhysicalEventFilter physicalFilter) throws Fabric3Exception {
        List<DataType> types = physicalFilter.getTypes();
        int i = 0;
        Class<?>[] classes = new Class<?>[types.size()];
        for (DataType type : types) {
            if (!(type instanceof JavaType)) {
                throw new Fabric3Exception("Unsupported data type: " + type);
            }
            classes[i] = type.getType();
        }
        return new JavaTypeEventFilter(classes);
    }
}
