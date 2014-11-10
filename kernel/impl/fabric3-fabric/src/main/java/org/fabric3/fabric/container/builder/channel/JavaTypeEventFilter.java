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

import org.fabric3.spi.container.builder.channel.EventFilter;

/**
 * Filters events based on a set of Java types.
 */
public class JavaTypeEventFilter implements EventFilter {
    private Class<?>[] types;

    /**
     * Constructor.
     *
     * @param types the types to filter on
     */
    public JavaTypeEventFilter(Class<?>... types) {
        this.types = types;
    }

    public boolean filter(Object object) {
        for (Class<?> type : types) {
            if (type.isAssignableFrom(object.getClass())) {
                return true;
            }
        }
        return false;
    }
}