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

import java.util.Collections;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;

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
                                         ClassLoader sourceLoader) throws Fabric3Exception {
        List<Class<?>> targetTypes = definition.getTargetParameterTypes();
        List<Class<?>> sourceTypes = definition.getSourceParameterTypes();
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
            throw new Fabric3Exception("No transformer found for operation: " + definition.getName());
        }

        // create the output transformer which flips the source and target types of the forward interceptor
        List<Class<?>> sourceOutTypes = Collections.singletonList(definition.getSourceReturnType());
        List<Class<?>> targetOutTypes = Collections.singletonList(definition.getTargetReturnType());
        Transformer<Object, Object> outTransformer = (Transformer<Object, Object>) registry.getTransformer(selectedTarget,
                                                                                                           selectedSource,
                                                                                                           targetOutTypes,
                                                                                                           sourceOutTypes);
        if (outTransformer == null) {
            throw new Fabric3Exception("No transformer from type " + selectedTarget + " to type " + selectedSource);
        }
        return new TransformerInterceptor(inTransformer, outTransformer, targetLoader, sourceLoader);
    }

}
