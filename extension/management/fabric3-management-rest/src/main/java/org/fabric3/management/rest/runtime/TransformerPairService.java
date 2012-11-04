/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.management.rest.runtime;

import java.lang.reflect.Method;
import java.util.List;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.transform.TransformationException;

/**
 * Returns a transformer pair for (de)serializing request/response types.
 */
public interface TransformerPairService {

    /**
     * Returns a transformer pair for serializing and deserializing request/response types for methods on a managed artifact.
     *
     * @param methods    the methods
     * @param inputType  the input (serialized) type
     * @param outputType the type responses should be serialized to
     * @return the pair
     * @throws TransformationException if an error occurs returning the pair
     */
    TransformerPair getTransformerPair(List<Method> methods, DataType<?> inputType, DataType<?> outputType) throws TransformationException;

}
