/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
        definition = new ComponentDefinition<TimerImplementation>(name);
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
