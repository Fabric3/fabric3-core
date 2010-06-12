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
package org.fabric3.binding.jms.generator;

import java.io.InputStream;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.binding.jms.spi.provision.PayloadType;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;

/**
 * Default implementation of the PayloadTypeIntrospector. Message types are determined as follows:
 * <pre>
 * <ul>
 * <li>If the parameters are primitives, the specific primitive type is returned
 * <li>If the parameters are a stream, a stream message is returned
 * <li>If the parameters are Serializable, an object message is returned
 * <li>If the parameter is annotated with @XmlRootElement or @XmlType, an XML type is returned
 * <ul>
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public class PayloadTypeIntrospectorImpl implements PayloadTypeIntrospector {

    public OperationPayloadTypes introspect(Operation operation) throws JmsGenerationException {
        List<DataType<?>> inputTypes = operation.getInputTypes();
        PayloadType inputType;
        if (inputTypes.size() == 1) {
            DataType<?> param = inputTypes.get(0);
            inputType = introspectType(param);
        } else {
            // more than one parameter, use an object type message
            inputType = PayloadType.OBJECT;
        }
        PayloadType outputType = introspectType(operation.getOutputType());
        if (outputType == PayloadType.XML) {
            // if output is XML, send faults as XML as well. Otherwise, send them as objects
            return new OperationPayloadTypes(operation.getName(), inputType, outputType, PayloadType.XML);
        } else {
            return new OperationPayloadTypes(operation.getName(), inputType, outputType, PayloadType.OBJECT);
        }
    }

    private PayloadType introspectType(DataType<?> param) throws JmsGenerationException {

        Class<?> physical = param.getPhysical();
        if (physical.isPrimitive() && !Void.TYPE.equals(physical)) {
            return calculatePrimitivePayloadType(physical);
        } else if (InputStream.class.isAssignableFrom(physical)) {
            return PayloadType.STREAM;
        } else if (String.class.isAssignableFrom(physical)) {
            return PayloadType.TEXT;
        } else if (physical.isAnnotationPresent(XmlRootElement.class) || physical.isAnnotationPresent(XmlType.class)) {
            return PayloadType.XML;
        }
        return PayloadType.OBJECT;
    }

    private PayloadType calculatePrimitivePayloadType(Class<?> clazz) throws JmsGenerationException {
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
            throw new JmsGenerationException("Parameter type not supported: " + clazz);
        }

    }

}
