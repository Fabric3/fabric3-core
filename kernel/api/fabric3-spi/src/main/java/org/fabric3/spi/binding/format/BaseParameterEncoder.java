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
package org.fabric3.spi.binding.format;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;

/**
 * Base class for WireFormatters that use custom serialization formats.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseParameterEncoder extends AbstractParameterEncoder {
    private ClassLoader loader;

    /**
     * Cosntructor
     *
     * @param loader the classloader defining message body content types
     */
    protected BaseParameterEncoder(ClassLoader loader) {
        this.loader = loader;
    }

    public byte[] encodeBytes(Message message) throws EncoderException {
        // serialize the body
        Object body = message.getBody();
        if (body != null && body.getClass().isArray() && !body.getClass().getComponentType().isPrimitive()) {
            Object[] array = (Object[]) body;
            if (array.length == 1) {
                body = array[0];
            } else if (array.length > 1) {
                throw new UnsupportedOperationException("Multiple paramters not supported");
            }
        }
        return serialize(body);
    }

    @Override
    public String encodeText(Message message) throws EncoderException {
        return Base64.encode(encodeBytes(message));
    }

    @Override
    public Object decode(String operationName, String body) throws EncoderException {
        return deserialize(Object.class, Base64.decode(body), loader);
    }

    @Override
    public Object decode(String operationName, byte[] serialized) throws EncoderException {
        return deserialize(Object.class, serialized, loader);
    }

    @Override
    public Object decodeResponse(String operationName, String serialized) throws EncoderException {
        return deserialize(Object.class, Base64.decode(serialized), loader);
    }

    @Override
    public Object decodeResponse(String operationName, byte[] serialized) throws EncoderException {
        return deserialize(Object.class, serialized, loader);
    }

    public Throwable decodeFault(String operationName, byte[] body) throws EncoderException {
        return deserialize(Throwable.class, body, loader);
    }

    public Throwable decodeFault(String operationName, String serialized) throws EncoderException {
        return decodeFault(operationName, Base64.decode(serialized));
    }

    protected abstract byte[] serialize(Object o) throws EncoderException;

    protected abstract <T> T deserialize(Class<T> clazz, byte[] bytes, ClassLoader cl) throws EncoderException;

}
