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
package org.fabric3.spi.model.type.system;

import org.fabric3.api.model.type.builder.ComponentDefinitionBuilder;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Builds a <code>implementation.system</code> component definition.
 */
public class SystemComponentDefinitionBuilder extends ComponentDefinitionBuilder<SystemComponentDefinitionBuilder> {
    private ComponentDefinition<SystemImplementation> definition;

    /**
     * Creates a new builder using the given component name and implementation class.
     *
     * @param name  the component name name
     * @param clazz the implementation class
     * @return the builder
     */
    public static SystemComponentDefinitionBuilder newBuilder(String name, Class<?> clazz) {
        return new SystemComponentDefinitionBuilder(name, clazz);
    }

    /**
     * Creates a new builder using the given implementation class. If the implementation class implements a single interface and ends in "Impl", its simple name
     * will be used as the component name. Otherwise, the implementation class name will be used.
     *
     * @param clazz the implementation class
     * @return the builder
     */
    public static SystemComponentDefinitionBuilder newBuilder(Class<?> clazz) {
        // derive the name: the interface name if there is one interface or the implementation name
        String name = clazz.getInterfaces().length == 1 && clazz.getName().endsWith("Impl") ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName();
        return new SystemComponentDefinitionBuilder(name, clazz);
    }

    public ComponentDefinition<SystemImplementation> build() {
        checkState();
        freeze();
        return definition;
    }

    @Override
    protected ComponentDefinition<?> getDefinition() {
        return definition;
    }

    protected SystemComponentDefinitionBuilder(String name, Class<?> clazz) {
        String className = clazz.getName();
        InjectingComponentType componentType = new InjectingComponentType(className);
        SystemImplementation implementation = new SystemImplementation(className);
        implementation.setComponentType(componentType);
        definition = new ComponentDefinition<>(name);
        definition.setImplementation(implementation);
    }

    private SystemComponentDefinitionBuilder implementation(Class<?> clazz) {
        String name = clazz.getName();
        definition.getImplementation().setImplementationClass(name);
        return this;
    }

}
