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
package org.fabric3.api.implementation.timer.builder;

import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerImplementation;
import org.fabric3.api.implementation.timer.model.TimerType;
import org.fabric3.api.model.type.builder.ComponentDefinitionBuilder;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Builds a <code>implementation.timer</code> component definition.
 */
public class TimerComponentDefinitionBuilder extends ComponentDefinitionBuilder<TimerComponentDefinitionBuilder> {
    private ComponentDefinition<TimerImplementation> definition;

    /**
     * Creates a new builder using the given component name and implementation class.
     *
     * @param name  the component name name
     * @param clazz the implementation class
     * @param type  the timer type
     * @return the builder
     */
    public static TimerComponentDefinitionBuilder newBuilder(String name, Class<?> clazz, TimerType type) {
        return new TimerComponentDefinitionBuilder(name, clazz, type).implementation(clazz);
    }

    /**
     * Creates a new builder using the given implementation class. If the implementation class implements a single interface, its simple name will be used as
     * the component name. Otherwise, the implementation class name will be used.
     *
     * @param clazz the implementation class
     * @param type  the timer type
     * @return the builder
     */
    public static TimerComponentDefinitionBuilder newBuilder(Class<?> clazz, TimerType type) {
        // derive the name: the interface name if there is one interface or the implementation name
        String name = clazz.getInterfaces().length == 1 ? clazz.getInterfaces()[0].getSimpleName() : clazz.getSimpleName();
        return new TimerComponentDefinitionBuilder(name, clazz, type).implementation(clazz);
    }

    public TimerComponentDefinitionBuilder fireOnce(long value) {
        checkState();
        TimerData data = definition.getImplementation().getTimerData();
        checkType(data, "fireOnce", TimerType.ONCE);
        data.setFireOnce(value);
        return this;
    }

    public TimerComponentDefinitionBuilder repeatInterval(long value) {
        checkState();
        TimerData data = definition.getImplementation().getTimerData();
        checkType(data, "repeatInterval", TimerType.INTERVAL);
        data.setRepeatInterval(value);
        return this;
    }

    public TimerComponentDefinitionBuilder fixedRate(long value) {
        checkState();
        TimerData data = definition.getImplementation().getTimerData();
        checkType(data, "fixedRate", TimerType.FIXED_RATE);
        data.setFixedRate(value);
        return this;
    }

    public TimerComponentDefinitionBuilder initialDelay(long value) {
        checkState();
        TimerData data = definition.getImplementation().getTimerData();
        data.setInitialDelay(value);
        return this;
    }

    public TimerComponentDefinitionBuilder poolName(String value) {
        checkState();
        TimerData data = definition.getImplementation().getTimerData();
        data.setPoolName(value);
        return this;
    }

    public ComponentDefinition<TimerImplementation> build() {
        checkState();
        freeze();
        return definition;
    }

    protected ComponentDefinition<?> getDefinition() {
        return definition;
    }

    protected TimerComponentDefinitionBuilder(String name, Class<?> clazz, TimerType type) {
        String className = clazz.getName();
        InjectingComponentType componentType = new InjectingComponentType(className);
        TimerImplementation implementation = new TimerImplementation();
        implementation.setImplementationClass(className);
        TimerData data = new TimerData();
        data.setType(type);
        implementation.setTimerData(data);
        implementation.setComponentType(componentType);
        definition = new ComponentDefinition<>(name);
        definition.setImplementation(implementation);
    }

    private TimerComponentDefinitionBuilder implementation(Class<?> clazz) {
        definition.getImplementation().setImplementationClass(clazz.getName());
        return this;
    }

    private void checkType(TimerData data, String attribute, TimerType expectedType) {
        TimerType type = data.getType();
        if (type != expectedType) {
            throw new IllegalArgumentException("Cannot set " + attribute + " for timer of type: " + type);
        }
    }

}
