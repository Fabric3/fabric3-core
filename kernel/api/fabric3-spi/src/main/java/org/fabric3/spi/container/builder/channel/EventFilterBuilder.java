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
package org.fabric3.spi.container.builder.channel;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.physical.PhysicalEventFilter;

/**
 * Creates an {@link EventFilter} from a {@link PhysicalEventFilter}.
 */
public interface EventFilterBuilder<T extends PhysicalEventFilter> {

    /**
     * Creates the filter.
     *
     * @param definition the filter definition.
     * @return the filter
     * @throws Fabric3Exception if there is an error creating the filter
     */
    EventFilter build(T definition) throws Fabric3Exception;

}