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
package org.fabric3.fabric.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.wire.InterceptorCreationException;
import org.fabric3.spi.wire.NoInterceptorException;
import org.fabric3.spi.wire.TransformerInterceptorFactory;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.wire.Interceptor;

/**
 *
 */
public class TransformerInterceptorFactoryImpl implements TransformerInterceptorFactory {
    private TransformerRegistry registry;

    public TransformerInterceptorFactoryImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked"})
    public Interceptor createInterceptor(PhysicalOperationDefinition definition,
                                         List<DataType<?>> sources,
                                         List<DataType<?>> targets,
                                         ClassLoader targetLoader,
                                         ClassLoader sourceLoader) throws InterceptorCreationException {
        List<Class<?>> targetTypes = loadTargetInputTypes(definition, targetLoader);
        List<Class<?>> sourceTypes = loadSourceInputTypes(definition, targetLoader);
        try {
            // Find a transformer that can convert from a type supported by the source component or binding to one supported by the target component
            // or binding. A search is performed by iterating the supported source and target types in order of preference.
            Transformer<Object, Object> inTransformer = null;
            DataType<?> selectedSource = null;
            DataType<?> selectedTarget = null;
            for (DataType<?> source : sources) {
                for (DataType<?> target : targets) {
                    inTransformer = (Transformer<Object, Object>) registry.getTransformer(source, target, sourceTypes, targetTypes);
                    if (inTransformer != null) {
                        selectedSource = source;
                        selectedTarget = target;
                        break;
                    }
                }
                if (selectedSource != null) {
                    // a transformer was found
                    break;
                }
            }
            if (inTransformer == null) {
                throw new NoInterceptorException("No transformer found for operation: " + definition.getName());
            }

            // create the output transformer which flips the source and target types of the forward interceptor
            List<Class<?>> sourceOutTypes = loadSourceOutputTypes(definition, targetLoader);
            List<Class<?>> targetOutTypes = loadTargetOutputTypes(definition, targetLoader);
            Transformer<Object, Object> outTransformer =
                    (Transformer<Object, Object>) registry.getTransformer(selectedTarget, selectedSource, targetOutTypes, sourceOutTypes);
            if (outTransformer == null) {
                throw new NoInterceptorException("No transformer from type " + selectedTarget + " to type " + selectedSource);
            }
            return new TransformerInterceptor(inTransformer, outTransformer, targetLoader, sourceLoader);
        } catch (TransformationException e) {
            throw new InterceptorCreationException(e);
        }
    }

    /**
     * Loads the source-side parameter types in the contribution classloader associated with the source component.
     *
     * @param definition the physical operation definition
     * @param loader     the  contribution classloader
     * @return a collection of loaded parameter types
     * @throws InterceptorCreationException
     *          if an error occurs loading the parameter types
     */
    private List<Class<?>> loadSourceInputTypes(PhysicalOperationDefinition definition, ClassLoader loader)
            throws InterceptorCreationException {
        try {
            return ParameterTypeHelper.loadSourceInParameterTypes(definition, loader);
        } catch (ClassNotFoundException e) {
            throw new InterceptorCreationException(e);
        }
    }

    /**
     * Loads the target-side parameter types in the contribution classloader associated with the target service.
     *
     * @param definition the physical operation definition
     * @param loader     the  contribution classloader
     * @return a collection of loaded parameter types
     * @throws InterceptorCreationException
     *          if an error occurs loading the parameter types
     */
    private List<Class<?>> loadTargetInputTypes(PhysicalOperationDefinition definition, ClassLoader loader)
            throws InterceptorCreationException {
        try {
            return ParameterTypeHelper.loadTargetInParameterTypes(definition, loader);
        } catch (ClassNotFoundException e) {
            throw new InterceptorCreationException(e);
        }
    }

    /**
     * Loads the source-side output parameter types in the contribution classloader associated of the source component.
     *
     * @param definition the physical operation definition
     * @param loader     the  contribution classloader
     * @return a collection of loaded parameter types
     * @throws InterceptorCreationException
     *          if an error occurs loading the parameter types
     */
    private List<Class<?>> loadSourceOutputTypes(PhysicalOperationDefinition definition, ClassLoader loader)
            throws InterceptorCreationException {
        List<Class<?>> types = new ArrayList<Class<?>>();
        try {
            Class<?> outParam = ParameterTypeHelper.loadSourceOutputType(definition, loader);
            types.add(outParam);
            // TODO handle fault types
            //  Set<Class<?>> faults = ParameterTypeHelper.loadFaultTypes(definition, loader);
            //  types.addAll(faults);
        } catch (ClassNotFoundException e) {
            throw new InterceptorCreationException(e);
        }
        return types;
    }

    /**
     * Loads the target-side output parameter types in the contribution classloader associated of the target service.
     *
     * @param definition the physical operation definition
     * @param loader     the  contribution classloader
     * @return a collection of loaded parameter types
     * @throws InterceptorCreationException
     *          if an error occurs loading the parameter types
     */
    private List<Class<?>> loadTargetOutputTypes(PhysicalOperationDefinition definition, ClassLoader loader)
            throws InterceptorCreationException {
        List<Class<?>> types = new ArrayList<Class<?>>();
        try {
            Class<?> outParam = ParameterTypeHelper.loadTargetOutputType(definition, loader);
            types.add(outParam);
            // TODO handle fault types
            //  Set<Class<?>> faults = ParameterTypeHelper.loadFaultTypes(definition, loader);
            //  types.addAll(faults);
        } catch (ClassNotFoundException e) {
            throw new InterceptorCreationException(e);
        }
        return types;
    }

}
