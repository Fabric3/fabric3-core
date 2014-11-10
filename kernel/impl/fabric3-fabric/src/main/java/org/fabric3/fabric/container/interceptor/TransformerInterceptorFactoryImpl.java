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
package org.fabric3.fabric.container.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.wire.InterceptorCreationException;
import org.fabric3.spi.container.wire.NoInterceptorException;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.container.wire.Interceptor;

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
                                         List<DataType> sources,
                                         List<DataType> targets,
                                         ClassLoader targetLoader,
                                         ClassLoader sourceLoader) throws InterceptorCreationException {
        List<Class<?>> targetTypes = loadTargetInputTypes(definition, targetLoader);
        List<Class<?>> sourceTypes = loadSourceInputTypes(definition, targetLoader);
        try {
            // Find a transformer that can convert from a type supported by the source component or binding to one supported by the target component
            // or binding. A search is performed by iterating the supported source and target types in order of preference.
            Transformer<Object, Object> inTransformer = null;
            DataType selectedSource = null;
            DataType selectedTarget = null;
            for (DataType source : sources) {
                for (DataType target : targets) {
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
        List<Class<?>> types = new ArrayList<>();
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
        List<Class<?>> types = new ArrayList<>();
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
