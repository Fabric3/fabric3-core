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
package org.fabric3.fabric.domain.instantiator;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Instantiates a logical component from a component definition.
 */
public interface AtomicComponentInstantiator {

    /**
     * Instantiates a logical component and any children from a component definition in the given parent context. Note the parent is updated with the
     * instantiated component.
     *
     * @param definition the component definition to instantiate from @return the instantiated logical component
     * @param parent     the parent logical component
     * @param context    the instantiation context
     * @return an instantiated logical component
     */
    LogicalComponent<?> instantiate(ComponentDefinition<?> definition, LogicalCompositeComponent parent, InstantiationContext context);

}