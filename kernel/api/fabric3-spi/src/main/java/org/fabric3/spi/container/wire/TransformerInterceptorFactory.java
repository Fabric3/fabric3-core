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
package org.fabric3.spi.container.wire;

import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;


/**
 * Creates interceptors that transform input and output parameters from one format to another, e.g. DOM to Java or vice versa.
 */
public interface TransformerInterceptorFactory {

    /**
     * Creates a transforming interceptor for a service operation. The interceptor converts input parameters from a source to a target type and ouput
     * parameters from a target to a source type. The source and target types are selected from the list of supported source and target types based on
     * order of preference (the source and target types are sorted in descending order) and the availability of a transformer.
     *
     * @param operation    the operation to create the interceptor for
     * @param sources      the source types in descending order of preference
     * @param targets      the supported target types, in descending order of preference
     * @param targetLoader the target service contribution classloader
     * @param sourceLoader the source component contribution classloader
     * @return the transforming interceptor
     * @throws InterceptorCreationException if there is an error creating the interceptor such a transformer not being available for any of the
     *                                      source-target type combinations
     */
    Interceptor createInterceptor(PhysicalOperationDefinition operation,
                                  List<DataType<?>> sources,
                                  List<DataType<?>> targets,
                                  ClassLoader targetLoader,
                                  ClassLoader sourceLoader) throws InterceptorCreationException;

}