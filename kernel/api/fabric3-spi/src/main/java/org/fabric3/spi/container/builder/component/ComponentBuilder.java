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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Builds a component for an implementation type on a runtime.
 */
public interface ComponentBuilder<D extends PhysicalComponentDefinition, C extends Component> {

    /**
     * Builds a component from its physical component definition.
     *
     * @param definition physical component definition of the component to be built
     * @return the component
     * @throws Fabric3Exception if unable to build the component
     */
    C build(D definition) throws Fabric3Exception;

    /**
     * Disposes a component.
     *
     * @param definition physical component definition of the component to be built.
     * @param component  the component
     * @throws Fabric3Exception if unable to build the component
     */
    void dispose(D definition, C component) throws Fabric3Exception;
}
