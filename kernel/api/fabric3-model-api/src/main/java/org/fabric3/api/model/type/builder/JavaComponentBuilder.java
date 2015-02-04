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

import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;

/**
 * Builds a <code>implementation.java</code> component definition.
 */
public class JavaComponentBuilder extends ComponentBuilder<JavaComponentBuilder> {
    private Component<JavaImplementation> component;

    /**
     * Creates a new builder using the given component name and implementation class.
     *
     * @param name  the component name name
     * @param clazz the implementation class
     * @return the builder
     */
    public static JavaComponentBuilder newBuilder(String name, Class<?> clazz) {
        return new JavaComponentBuilder(name, clazz).implementation(clazz);
    }

    /**
     * Creates a new builder using the given implementation class. If the implementation class implements a single interface, its simple name will be used as
     * the component name. Otherwise, the implementation class name will be used.
     *
     * @param clazz the implementation class
     * @return the builder
     */
    public static JavaComponentBuilder newBuilder(Class<?> clazz) {
        // derive the name: the interface name if there is one interface or the implementation name
        String name = clazz.getInterfaces().length == 1 ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName();
        return new JavaComponentBuilder(name, clazz).implementation(clazz);
    }

    /**
     * Creates a new builder.
     *
     * @param name     the component name
     * @param instance the component instance
     * @return the builder
     */
    public static JavaComponentBuilder newBuilder(String name, Object instance) {
        return new JavaComponentBuilder(name, instance);
    }

    public Component<JavaImplementation> build() {
        checkState();
        freeze();
        return component;
    }

    @Override
    protected Component<?> getComponent() {
        return component;
    }

    protected JavaComponentBuilder(String name, Object instance) {
        Class<?> clazz = instance.getClass();
        String className = clazz.getName();
        InjectingComponentType componentType = new InjectingComponentType(clazz);
        JavaImplementation implementation = new JavaImplementation(instance);
        implementation.setComponentType(componentType);
        implementation.setImplementationClass(clazz);
        component = new Component<>(name);
        component.setImplementation(implementation);
    }

    protected JavaComponentBuilder(String name, Class<?> clazz) {
        String className = clazz.getName();
        InjectingComponentType componentType = new InjectingComponentType(clazz);
        JavaImplementation implementation = new JavaImplementation();
        implementation.setImplementationClass(clazz);
        implementation.setComponentType(componentType);
        component = new Component<>(name);
        component.setImplementation(implementation);
    }

    private JavaComponentBuilder implementation(Class<?> clazz) {
        component.getImplementation().setImplementationClass(clazz);
        return this;
    }

}
