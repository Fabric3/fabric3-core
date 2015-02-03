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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.generator;

import java.io.InputStream;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.binding.jms.spi.provision.PayloadType;

/**
 * Default implementation of the PayloadTypeIntrospector.
 *
 * JMS Payload types are mapped as follows:
 * <pre>
 * <ul>
 * <li>If the data type are primitives, the specific primitive payload type is returned
 * <li>If the data type are a stream, a stream payload type is returned
 * <li>If the data type is a String, a text payload type is returned
 * <li>If the data type has an associated JAXB databinding, a text payload type is returned
 * <li>Otherwise an object payload type is returned
 * <ul>
 * </pre>
 *
 * Note that this implementation currently supports mapping JAXB data types; additional data types such as Protobufs could be supported in the future.
 */
public class PayloadTypeIntrospectorImpl implements PayloadTypeIntrospector {
    private static final String JAXB = "JAXB";

    public OperationPayloadTypes introspect(Operation operation) throws Fabric3Exception {
        PayloadType inputType = getInputPayloadType(operation);
        PayloadType outputType = introspectType(operation.getOutputType());
        PayloadType faultType = getFaultPayloadType(operation);
        return new OperationPayloadTypes(operation.getName(), inputType, outputType, faultType);
    }

    private PayloadType getInputPayloadType(Operation operation) throws Fabric3Exception {
        List<DataType> inputTypes = operation.getInputTypes();
        if (inputTypes.size() == 1) {
            DataType param = inputTypes.get(0);
            return introspectType(param);
        } else {
            // more than one parameter, use an object type message
            return PayloadType.OBJECT;
        }
    }

    private PayloadType getFaultPayloadType(Operation operation) throws Fabric3Exception {
        for (DataType dataType : operation.getFaultTypes()) {
            if ("JAXB".equals(dataType.getDatabinding())) {
                return PayloadType.TEXT;
            }
        }
        return PayloadType.OBJECT;
    }

    private PayloadType introspectType(DataType param) throws Fabric3Exception {

        Class<?> type = param.getType();
        if (type.isPrimitive() && !Void.TYPE.equals(type)) {
            return calculatePrimitivePayloadType(type);
        } else if (InputStream.class.isAssignableFrom(type)) {
            return PayloadType.STREAM;
        } else if (String.class.isAssignableFrom(type) || JAXB.equals(param.getDatabinding())) {
            return PayloadType.TEXT;
        }
        return PayloadType.OBJECT;
    }

    private PayloadType calculatePrimitivePayloadType(Class<?> clazz) throws Fabric3Exception {
        if (Short.TYPE.equals(clazz)) {
            return PayloadType.SHORT;
        } else if (Integer.TYPE.equals(clazz)) {
            return PayloadType.INTEGER;
        } else if (Double.TYPE.equals(clazz)) {
            return PayloadType.DOUBLE;
        } else if (Float.TYPE.equals(clazz)) {
            return PayloadType.FLOAT;
        } else if (Long.TYPE.equals(clazz)) {
            return PayloadType.LONG;
        } else if (Character.TYPE.equals(clazz)) {
            return PayloadType.CHARACTER;
        } else if (Boolean.TYPE.equals(clazz)) {
            return PayloadType.BOOLEAN;
        } else if (Byte.TYPE.equals(clazz)) {
            return PayloadType.BYTE;
        } else {
            throw new Fabric3Exception("Parameter type not supported: " + clazz);
        }

    }

}
