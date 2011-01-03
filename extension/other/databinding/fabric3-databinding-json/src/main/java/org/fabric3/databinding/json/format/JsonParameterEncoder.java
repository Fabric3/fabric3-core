/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.databinding.json.format;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.binding.format.AbstractParameterEncoder;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.invocation.Message;

/**
 * ParameterEncoder that uses JSON. Note this implementation only encodes type information for faults, wrapping them in {@link ApplicationFault}.
 *
 * @version $Rev$ $Date$
 */
public class JsonParameterEncoder extends AbstractParameterEncoder {
    private ObjectMapper mapper;
    private Map<String, OperationTypes> mappings;
    private Map<String, Constructor<?>> faultCtors;

    public JsonParameterEncoder(Map<String, OperationTypes> mappings) throws EncoderException {
        this.mappings = mappings;
        this.mapper = new ObjectMapper();
        faultCtors = new HashMap<String, Constructor<?>>();
        for (OperationTypes types : mappings.values()) {
            for (Class<?> faultType : types.getFaultTypes()) {
                try {
                    if (faultType.isAssignableFrom(Throwable.class)) {
                        throw new IllegalArgumentException("Fault must be a Throwable: " + faultType.getName());
                    }
                    Constructor<?> ctor = faultType.getConstructor(String.class);
                    faultCtors.put(faultType.getSimpleName(), ctor);
                } catch (NoSuchMethodException e) {
                    throw new EncoderException("Fault type must contain a public constructor taking a String message: " + faultType.getName());
                }
            }
        }
    }


    public String encodeText(Message message) throws EncoderException {
        Object body = message.getBody();
        if (message.isFault()) {
            Throwable exception = (Throwable) message.getBody();
            ApplicationFault fault = new ApplicationFault();
            fault.setMessage(exception.getMessage());
            fault.setType(exception.getClass().getSimpleName());
            body = fault;
        } else {
            if (body != null && body.getClass().isArray() && !body.getClass().isPrimitive()) {
                Object[] payload = (Object[]) body;
                if (payload.length > 1) {
                    throw new UnsupportedOperationException("Multiple paramters not supported");
                }
                body = payload[0];
            }
        }
        try {
            StringWriter writer = new StringWriter();
            mapper.writeValue(writer, body);
            return writer.toString();
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public byte[] encodeBytes(Message message) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Object decode(String operationName, String serialized) throws EncoderException {
        try {
            OperationTypes types = mappings.get(operationName);
            if (types == null) {
                throw new EncoderException("Operation not found: " + operationName);
            }

            Class<?> inType = types.getInParameterType();
            if (inType != null) {
                return mapper.readValue(serialized, inType);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }

    }

    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        try {
            OperationTypes types = mappings.get(operationName);
            if (types == null) {
                throw new EncoderException("Operation not found: " + operationName);
            }
            Class<?> inType = types.getInParameterType();
            if (inType != null) {
                return mapper.readValue(serialized, inType);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public Throwable decodeFault(String operationName, String serialized) throws EncoderException {
        ApplicationFault fault;
        try {
            fault = mapper.readValue(serialized, ApplicationFault.class);
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        Constructor<?> ctor = faultCtors.get(fault.getType());

        if (ctor == null) {
            return new ServiceRuntimeException("Unknown fault thrown by service. Type is " + fault.getType() + ". Message:" + fault.getMessage());
        }

        try {
            return (Throwable) ctor.newInstance(fault.getMessage());
        } catch (InstantiationException e) {
            throw new EncoderException(e);
        } catch (IllegalAccessException e) {
            throw new EncoderException(e);
        } catch (InvocationTargetException e) {
            throw new EncoderException(e);
        }
    }


}