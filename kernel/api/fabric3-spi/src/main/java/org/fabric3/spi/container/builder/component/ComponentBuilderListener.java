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
package org.fabric3.spi.container.builder.component;

import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Receives callbacks when a component is built.
 */
public interface ComponentBuilderListener {

    /**
     * Callback when a component is built.
     *
     * @param component  the built component
     * @param definition physical component definition of the component being built.
     */
    void onBuild(Component component, PhysicalComponentDefinition definition);

    /**
     * Callback when a component is disposed.
     *
     * @param component  the built component
     * @param definition physical component definition of the component being disposed.
     */
    void onDispose(Component component, PhysicalComponentDefinition definition);
}
