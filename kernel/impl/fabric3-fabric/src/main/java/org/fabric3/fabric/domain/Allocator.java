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
package org.fabric3.fabric.domain;

import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * Allocates components and channels to a zone. If the component is a composite, allocation will be performed recursively.
 */
public interface Allocator {

    /**
     * Allocates a component. Composites are recursed and their children are allocated.
     *
     * @param component the component to allocate
     */
    void allocate(LogicalComponent<?> component);

    /**
     * Allocates a channel. Composites are recursed and their children are allocated.
     *
     * @param channel the channel to allocate
     */
    void allocate(LogicalChannel channel);

    /**
     * Allocates a resource. Composites are recursed and their children are allocated.
     *
     * @param resource the resource to allocate
     */
    void allocate(LogicalResource<?> resource);

}
