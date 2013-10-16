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
package org.fabric3.fabric.container.handler;

import java.util.Collections;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.builder.WiringException;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.HandlerCreationException;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
public class TransformerHandlerFactoryImpl implements TransformerHandlerFactory {
    private TransformerRegistry registry;

    public TransformerHandlerFactoryImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked"})
    public EventStreamHandler createHandler(DataType<?> source, DataType<?> target, ClassLoader loader) throws HandlerCreationException {
        try {
            // Find a transformer that can convert from a type supported by the source component or binding to one supported by the target component
            // or binding. A search is performed by iterating the supported source and target types in order of preference.
            List<Class<?>> empty = Collections.emptyList();
            Transformer<Object, Object> transformer = (Transformer<Object, Object>) registry.getTransformer(source, target, empty, empty);
            if (transformer == null) {
                throw new NoTransformerException("No transformer found for event types: " + source + "," + target);
            }
            return new TransformerHandler(transformer, loader);
        } catch (TransformationException e) {
            throw new HandlerCreationException(e);
        }
    }

    /**
     * Loads the source-side parameter types in the contribution classloader associated with the source component.
     *
     * @param definition the physical operation definition
     * @param loader     the  contribution classloader
     * @return a collection of loaded parameter types
     * @throws WiringException if an error occurs loading the parameter types
     */
    private List<Class<?>> loadSourceInputTypes(PhysicalOperationDefinition definition, ClassLoader loader) throws WiringException {
        try {
            return ParameterTypeHelper.loadSourceInParameterTypes(definition, loader);
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
    }

    /**
     * Loads the target-side parameter types in the contribution classloader associated with the target service.
     *
     * @param definition the physical operation definition
     * @param loader     the  contribution classloader
     * @return a collection of loaded parameter types
     * @throws WiringException if an error occurs loading the parameter types
     */
    private List<Class<?>> loadTargetInputTypes(PhysicalOperationDefinition definition, ClassLoader loader) throws WiringException {
        try {
            return ParameterTypeHelper.loadTargetInParameterTypes(definition, loader);
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }
    }


}
