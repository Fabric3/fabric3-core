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
package org.fabric3.api.model.type.builder;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;

/**
 * Builds a <code>implementation.java</code> component definition.
 */
public class JavaComponentDefinitionBuilder extends ComponentDefinitionBuilder<JavaComponentDefinitionBuilder> {
    private ComponentDefinition<JavaImplementation> definition;

    /**
     * Creates a new builder using the given component name and implementation class.
     *
     * @param name  the component name name
     * @param clazz the implementation class
     * @return the builder
     */
    public static JavaComponentDefinitionBuilder newBuilder(String name, Class<?> clazz) {
        return new JavaComponentDefinitionBuilder(name, clazz).implementation(clazz);
    }

    /**
     * Creates a new builder using the given implementation class. If the implementation class implements a single interface, its simple name will be used as
     * the component name. Otherwise, the implementation class name will be used.
     *
     * @param clazz the implementation class
     * @return the builder
     */
    public static JavaComponentDefinitionBuilder newBuilder(Class<?> clazz) {
        // derive the name: the interface name if there is one interface or the implementation name
        String name = clazz.getInterfaces().length == 1 ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName();
        return new JavaComponentDefinitionBuilder(name, clazz).implementation(clazz);
    }

    /**
     * Creates a new builder.
     *
     * @param name     the component name
     * @param instance the component instance
     * @return the builder
     */
    public static JavaComponentDefinitionBuilder newBuilder(String name, Object instance) {
        return new JavaComponentDefinitionBuilder(name, instance);
    }

    public ComponentDefinition<JavaImplementation> build() {
        checkState();
        freeze();
        return definition;
    }

    @Override
    protected ComponentDefinition<?> getDefinition() {
        return definition;
    }

    protected JavaComponentDefinitionBuilder(String name, Object instance) {
        Class<?> clazz = instance.getClass();
        String className = clazz.getName();
        InjectingComponentType componentType = new InjectingComponentType(className);
        JavaImplementation implementation = new JavaImplementation(instance);
        implementation.setComponentType(componentType);
        implementation.setImplementationClass(className);
        definition = new ComponentDefinition<>(name);
        definition.setImplementation(implementation);
    }

    protected JavaComponentDefinitionBuilder(String name, Class<?> clazz) {
        String className = clazz.getName();
        InjectingComponentType componentType = new InjectingComponentType(className);
        JavaImplementation implementation = new JavaImplementation();
        implementation.setImplementationClass(className);
        implementation.setComponentType(componentType);
        definition = new ComponentDefinition<>(name);
        definition.setImplementation(implementation);
    }

    private JavaComponentDefinitionBuilder implementation(Class<?> clazz) {
        definition.getImplementation().setImplementationClass(clazz.getName());
        return this;
    }

}
