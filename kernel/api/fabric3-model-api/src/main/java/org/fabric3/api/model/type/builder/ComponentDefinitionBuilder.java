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
 * Creates {@link ComponentDefinition}s for deploying instances.
 */
public class ComponentDefinitionBuilder {
    private String name;
    private Object instance;

    /**
     * Creates a builder.
     *
     * @param name     the component name
     * @param instance the component instances
     * @return the builder
     */
    public static ComponentDefinitionBuilder newBuilder(String name, Object instance) {
        return new ComponentDefinitionBuilder(name, instance);
    }

    public ComponentDefinitionBuilder(String name, Object instance) {
        this.name = name;
        this.instance = instance;
    }

    /**
     * Builds the component definition.
     *
     * @return the component definition
     */
    public ComponentDefinition<JavaImplementation> build() {
        ComponentDefinition<JavaImplementation> definition = new ComponentDefinition<JavaImplementation>(name);
        JavaImplementation implementation = new JavaImplementation(instance);
        InjectingComponentType type = new InjectingComponentType(instance.getClass().getName());
        implementation.setComponentType(type);
        definition.setImplementation(implementation);
        return definition;
    }

}
