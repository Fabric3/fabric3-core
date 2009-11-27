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
*/
package org.fabric3.spi.binding.format;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.util.Base64;

/**
 * Base class for MessageEncoders that use custom serialization formats.
 *
 * @version $Rev$ $Date$
 */
public abstract class BaseMessageEncoder implements MessageEncoder {

    public String encodeText(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        return Base64.encode(encode(message));
    }

    public byte[] encodeBytes(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        return encode(message);
    }

    public String encodeResponseText(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        String serialized = Base64.encode(encode(message));
        callback.encodeContentLengthHeader(serialized.length());
        return serialized;
    }

    public byte[] encodeResponseBytes(String operationName, Message message, ResponseEncodeCallback callback)
            throws EncoderException {
        byte[] serialized = encode(message);
        callback.encodeContentLengthHeader(serialized.length);
        return serialized;
    }

    public Message decodeResponse(byte[] serialized) throws EncoderException {
        return decode(Message.class, serialized);
    }

    public Message decodeResponse(String serialized) throws EncoderException {
        return decode(Message.class, Base64.decode(serialized));
    }

    public Message decodeFault(byte[] serialized) throws EncoderException {
        return decode(Message.class, serialized);
    }

    public Message decodeFault(String serialized) throws EncoderException {
        return decode(Message.class, Base64.decode(serialized));
    }


    public Message decode(byte[] serialized, HeaderContext context) throws EncoderException {
        return decode(Message.class, serialized);
    }

    public Message decode(String serialized, HeaderContext context) throws EncoderException {
        return decode(Message.class, Base64.decode(serialized));
    }

    /**
     * Encodes an object as a byte array.
     *
     * @param o the object to encode
     * @return the encoded object
     * @throws EncoderException if an encoding error occurs
     */
    protected abstract byte[] encode(Object o) throws EncoderException;

    /**
     * Decodes an object from a byte array.
     *
     * @param clazz the expected type
     * @param bytes byte array to decode
     * @return the decoded object
     * @throws EncoderException if an decoding error occurs
     */
    protected abstract <T> T decode(Class<T> clazz, byte[] bytes) throws EncoderException;


}