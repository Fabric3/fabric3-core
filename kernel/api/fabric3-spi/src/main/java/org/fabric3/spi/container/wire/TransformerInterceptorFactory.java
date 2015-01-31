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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.container.wire;

import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;


/**
 * Creates interceptors that transform input and output parameters from one format to another, e.g. DOM to Java or vice versa.
 */
public interface TransformerInterceptorFactory {

    /**
     * Creates a transforming interceptor for a service operation. The interceptor converts input parameters from a source to a target type and output
     * parameters from a target to a source type. The source and target types are selected from the list of supported source and target types based on
     * order of preference (the source and target types are sorted in descending order) and the availability of a transformer.
     *
     * @param operation    the operation to create the interceptor for
     * @param sources      the source types in descending order of preference
     * @param targets      the supported target types, in descending order of preference
     * @param targetLoader the target service contribution classloader
     * @param sourceLoader the source component contribution classloader
     * @return the transforming interceptor
     * @throws ContainerException if there is an error creating the interceptor such a transformer not being available for any of the
     *                                      source-target type combinations
     */
    Interceptor createInterceptor(PhysicalOperationDefinition operation,
                                  List<DataType> sources,
                                  List<DataType> targets,
                                  ClassLoader targetLoader,
                                  ClassLoader sourceLoader) throws ContainerException;

}