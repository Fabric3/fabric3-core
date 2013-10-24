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
        definition = new ComponentDefinition<SystemImplementation>(name);
        definition.setImplementation(implementation);
    }

    private SystemComponentDefinitionBuilder implementation(Class<?> clazz) {
        String name = clazz.getName();
        definition.getImplementation().setImplementationClass(name);
        return this;
    }

}
