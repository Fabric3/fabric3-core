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
        return new JavaComponentDefinitionBuilder(name).implementation(clazz);
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
        return new JavaComponentDefinitionBuilder(name).implementation(clazz);
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

    protected JavaComponentDefinitionBuilder(String name, Object instance) {
        Class<?> clazz = instance.getClass();
        InjectingComponentType componentType = new InjectingComponentType();
        JavaImplementation implementation = new JavaImplementation(instance);
        implementation.setComponentType(componentType);
        implementation.setImplementationClass(clazz.getName());
        definition = new ComponentDefinition<JavaImplementation>(name);
        definition.setImplementation(implementation);
    }

    protected JavaComponentDefinitionBuilder(String name) {
        InjectingComponentType componentType = new InjectingComponentType();
        JavaImplementation implementation = new JavaImplementation();
        implementation.setComponentType(componentType);
        definition = new ComponentDefinition<JavaImplementation>(name);
        definition.setImplementation(implementation);
    }

    public ComponentDefinition<JavaImplementation> build() {
        return definition;
    }

    @Override
    protected ComponentDefinition<?> getDefinition() {
        return definition;
    }

    private JavaComponentDefinitionBuilder implementation(Class<?> clazz) {
        definition.getImplementation().setImplementationClass(clazz.getName());
        return this;
    }

}
