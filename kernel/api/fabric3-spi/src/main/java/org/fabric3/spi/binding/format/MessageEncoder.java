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

/**
 * Encodes a message for a particular wire format. A wire format is written and read using a {@link ParameterEncoder} and MessageEncoder pair. The
 * parameter encoder is responsible for reading and writing in-, out- and fault types. The MessageEncoder is responsible for reading and writing the
 * message envelope.
 * <p/>
 * Writing is a two-step process. The ParameterEncoder is invoked to encode parameters, which are then set in encoded form in the message body. The
 * MessageEncoder is then used to encode the message envelope. Reading reverses this process. The MessageEncoder is invoked to decode the message
 * envelope, followed by the ParameterEncoder, which will use the target component classloader to read parameters.
 * <p/>
 * During encoding, clients may receive callbacks to handle writing message data such as headers. MessageEncoder implementations that encode all
 * message metadata in the envelope may not issue callbacks. However, certain message encoding formats propagate message data as part of the transport
 * "packet" (e.g. HTTP headers and JMS message propertes). The callback mechanism is used to notify the client to write to transport packet.
 *
 * @version $Rev$ $Date$
 */
public interface MessageEncoder {

    /**
     * Encodes a message in text format for an operation invocation.
     *
     * @param operationName the name of the operation being invoked.
     * @param message       the message with encoded invocation parameters
     * @param callback      the callback object to receive encoding events
     * @return the encoded message
     * @throws EncoderException if an error encoding the message occurs
     */
    String encodeText(String operationName, Message message, EncodeCallback callback) throws EncoderException;

    /**
     * Encodes a message in byte format for an operation invocation.
     *
     * @param operationName the name of the operation being invoked.
     * @param message       the message with encoded invocation parameters
     * @param callback      the callback object to receive encoding events
     * @return the encoded message
     * @throws EncoderException if an error encoding the message occurs
     */
    byte[] encodeBytes(String operationName, Message message, EncodeCallback callback) throws EncoderException;

    /**
     * Encodes a response message in text format for an operation invocation.
     *
     * @param operationName the name of the operation being invoked.
     * @param message       the message with encoded return parameters (or fault)
     * @param callback      the callback object to receive encoding events
     * @return the encoded message
     * @throws EncoderException if an error encoding the message occurs
     */
    String encodeResponseText(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException;

    /**
     * Encodes a response message in byte format for an operation invocation.
     *
     * @param operationName the name of the operation being invoked.
     * @param message       the message with encoded return parameters (or fault)
     * @param callback      the callback object to receive encoding events
     * @return the encoded message
     * @throws EncoderException if an error encoding the message occurs
     */
    byte[] encodeResponseBytes(String operationName, Message message, ResponseEncodeCallback callback) throws EncoderException;

    /**
     * Decodes a message for an operation invocation. This is called prior on the service provider side of a wire to deserialize invocation data.
     *
     * @param encoded the encoded message as a byte array
     * @param context provides transport-indepdent contextual information required when decoding the message
     * @return the decoded message
     * @throws EncoderException if an error decoding the message occurs
     */
    Message decode(byte[] encoded, HeaderContext context) throws EncoderException;

    /**
     * Decodes a message for an operation invocation. This is called prior on the service provider side of a wire to deserialize invocation data.
     *
     * @param encoded the encoded message as a string
     * @param context provides transport-indepdent contextual information required when decoding the message
     * @return the decoded message
     * @throws EncoderException if an error decoding the message occurs
     */
    Message decode(String encoded, HeaderContext context) throws EncoderException;

    /**
     * Decodes a message for an invocation response. This is called prior on the client side of a wire to deserialize response data.
     *
     * @param encoded the encoded message as a byte array
     * @return the decoded message
     * @throws EncoderException if an error decoding the message occurs
     */
    Message decodeResponse(byte[] encoded) throws EncoderException;

    /**
     * Decodes a message for an invocation response. This is called prior on the client side of a wire to deserialize response data.
     *
     * @param encoded the encoded message as a string
     * @return the decoded message
     * @throws EncoderException if an error decoding the message occurs
     */
    Message decodeResponse(String encoded) throws EncoderException;

    /**
     * Decodes a fault message for an invocation response. This is called prior on the client side of a wire to deserialize response fault.
     *
     * @param encoded the encoded message as a byte array
     * @return the decoded message
     * @throws EncoderException if an error decoding the message occurs
     */
    Message decodeFault(byte[] encoded) throws EncoderException;

    /**
     * Decodes a fault message for an invocation response. This is called prior on the client side of a wire to deserialize response fault.
     *
     * @param encoded the encoded message as a string
     * @return the decoded message
     * @throws EncoderException if an error decoding the message occurs
     */
    Message decodeFault(String encoded) throws EncoderException;

}
