/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.generator;

import javax.xml.namespace.QName;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.InterceptorGenerator;
import org.fabric3.spi.generator.ResourceGenerator;
import org.fabric3.spi.generator.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * A registry for {@link ComponentGenerator}s, {@link BindingGenerator}s, {@link ConnectionBindingGenerator}s, and {@link InterceptorGenerator}s .
 * Generators are responsible for producing physical model objects that are provisioned to service nodes from their logical counterparts.
 *
 * @version $Rev$ $Date$
 */
public interface GeneratorRegistry {

    /**
     * Returns a component generator for the specified implementation.
     *
     * @param clazz the implementation type the generator handles.
     * @return a the component generator for that implementation type
     * @throws GeneratorNotFoundException if no generator is registered for the implementation type
     */
    <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Returns a binding generator for the specified binding class.
     *
     * @param clazz The binding type type the generator handles.
     * @return The registered binding generator.
     * @throws GeneratorNotFoundException if no generator is registered for the binding type
     */
    <T extends BindingDefinition> BindingGenerator<T> getBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Returns a connection binding generator for the specified binding class.
     *
     * @param clazz The binding type type the generator handles.
     * @return The registered binding generator.
     * @throws GeneratorNotFoundException if no generator is registered for the binding type
     */
    <T extends BindingDefinition> ConnectionBindingGenerator<?> getConnectionBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Returns the resource reference generator for the resource type.
     *
     * @param clazz the resource type the generator handles
     * @return the resource reference generator
     * @throws GeneratorNotFoundException if no generator is registered for the resource type
     */
    <T extends ResourceReferenceDefinition> ResourceReferenceGenerator<T> getResourceReferenceGenerator(Class<T> clazz)
            throws GeneratorNotFoundException;

    /**
     * Returns the resource generator for the resource type.
     *
     * @param clazz the resource type the generator handles
     * @return the resource generator
     * @throws GeneratorNotFoundException if no generator is registered for the resource type
     */
    <T extends ResourceDefinition> ResourceGenerator<T> getResourceGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Returns the interceptor definition generator for the qualified name.
     *
     * @param extensionName qualified name of the policy extension
     * @return interceptor generator
     * @throws GeneratorNotFoundException if no generator is registered for the policy extension type
     */
    InterceptorGenerator getInterceptorDefinitionGenerator(QName extensionName) throws GeneratorNotFoundException;

}
