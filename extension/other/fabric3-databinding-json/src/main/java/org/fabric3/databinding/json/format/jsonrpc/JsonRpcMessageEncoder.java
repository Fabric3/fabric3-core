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
package org.fabric3.databinding.json.format.jsonrpc;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;

import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.HeaderContext;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ResponseEncodeCallback;
import org.fabric3.spi.invocation.Message;

/**
 * Serializer that reads and writes data using JSON RPC.
 * <p/>
 * <b> Note this implementation is note yet complete.
 *
 * @version $Rev$ $Date$
 */
public class JsonRpcMessageEncoder implements MessageEncoder {
    private ObjectMapper mapper;

    public JsonRpcMessageEncoder() throws EncoderException {
        this.mapper = new ObjectMapper();
    }

    public String encodeText(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        StringWriter writer = new StringWriter();
        try {
            JsonRpcRequest request;
            Object[] params = (Object[]) message.getBody();
            if (params == null) {
                request = new JsonRpcRequest(UUID.randomUUID().toString(), operationName);
            } else {
                List<Object> paramArray = Arrays.asList(params);
                request = new JsonRpcRequest(UUID.randomUUID().toString(), operationName, paramArray);
            }
            mapper.writeValue(writer, request);
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        return writer.toString();
    }

    public byte[] encodeBytes(String operationName, Message message, EncodeCallback callback) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public String encodeResponseText(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        StringWriter writer = new StringWriter();
        // FIXME the correlation id should be the id of the original request
        String correlationId = "";
        try {
            JsonRpcResponse request;
            Object result = message.getBody();
            if (result == null) {
                request = new JsonRpcResponse(correlationId);
            } else {
                request = new JsonRpcResponse(correlationId, result);
            }
            mapper.writeValue(writer, request);
        } catch (IOException e) {
            throw new EncoderException(e);
        }

        return writer.toString();
    }

    public byte[] encodeResponseBytes(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Message decode(byte[] encoded, HeaderContext context) throws EncoderException {
        throw new UnsupportedOperationException();
    }

    public Message decode(String encoded, HeaderContext context) throws EncoderException {
        try {
            JsonRpcRequest request = mapper.readValue(encoded, JsonRpcRequest.class);
            return null;
        } catch (IOException e) {
            throw new EncoderException(e);
        }
    }

    public Message decodeResponse(byte[] encoded) throws EncoderException {
        return null;
    }

    public Message decodeResponse(String encoded) throws EncoderException {
        return null;
    }

    public Message decodeFault(byte[] encoded) throws EncoderException {
        return null;
    }

    public Message decodeFault(String encoded) throws EncoderException {
        return null;
    }
}