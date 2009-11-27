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
 * Encodes in-, out- and fault parameters.
 *
 * @version $Rev$ $Date$
 */
public interface ParameterEncoder {

    /**
     * Encodes the parameters for a service invocation as a string.
     *
     * @param message the invocation message
     * @return the encoded parameters
     * @throws EncoderException if an encoding error occurs
     */
    String encodeText(Message message) throws EncoderException;

    /**
     * Encodes the parameters for a service invocation as a byte array.
     *
     * @param message the invocation message
     * @return the encoded parameters
     * @throws EncoderException if an encoding error occurs
     */
    byte[] encodeBytes(Message message) throws EncoderException;

    /**
     * Decodes the parameters for a service invocation  encoded as a string on the service provider side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded parameters
     * @return the decoded parameters
     * @throws EncoderException if a decoding error occurs
     */
    Object decode(String operationName, String encoded) throws EncoderException;

    /**
     * Decodes the parameters for a service invocation encoded as a byte array on the service provider side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded parameters
     * @return the decoded parameters
     * @throws EncoderException if a decoding error occurs
     */
    Object decode(String operationName, byte[] encoded) throws EncoderException;

    /**
     * Decodes a service invocation response as a string on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded response
     * @return the decoded response
     * @throws EncoderException if a decoding error occurs
     */
    Object decodeResponse(String operationName, String encoded) throws EncoderException;

    /**
     * Decodes a service invocation response as a byte array on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded response
     * @return the decoded response
     * @throws EncoderException if a decoding error occurs
     */
    Object decodeResponse(String operationName, byte[] encoded) throws EncoderException;

    /**
     * Decodes a service invocation fault response as a string on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded fault
     * @return the decoded fault
     * @throws EncoderException if a decoding error occurs
     */
    Throwable decodeFault(String operationName, String encoded) throws EncoderException;

    /**
     * Decodes a service invocation fault response as a byte array on the client side of a wire.
     *
     * @param operationName the name of the operation being invoked
     * @param encoded       the encoded fault
     * @return the decoded fault
     * @throws EncoderException if a decoding error occurs
     */
    Throwable decodeFault(String operationName, byte[] encoded) throws EncoderException;

}