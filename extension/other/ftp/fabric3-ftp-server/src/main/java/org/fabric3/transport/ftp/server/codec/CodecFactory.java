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
package org.fabric3.transport.ftp.server.codec;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

/**
 * Protocol encoder and decoder factory for inbound and outbound messages.
 *
 * @version $Rev$ $Date$
 */
public class CodecFactory implements ProtocolCodecFactory {

    private static Charset CHARSET = Charset.forName("UTF-8");
    private static CharsetEncoder CHARSET_ENCODER = CHARSET.newEncoder();

    private ProtocolDecoder decoder = new TextLineDecoder(CHARSET);
    private ProtocolEncoder encoder = new ResponseEncoder();

    /**
     * Gets the protocol decoder.
     *
     * @param session Session for which the decoder is created.
     * @return Protocol decoder.
     */
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    /**
     * Gets the protocol encoder.
     *
     * @param session Session for which the encoder is created.
     * @return Protocol encoder.
     */
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }

    /*
    * Response encoder.
    */
    private class ResponseEncoder extends ProtocolEncoderAdapter {

        public void encode(IoSession session, Object message, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {

            String stringMessage = message.toString();
            IoBuffer buffer = IoBuffer.allocate(stringMessage.length()).setAutoExpand(true);
            buffer.putString(stringMessage, CHARSET_ENCODER);

            buffer.flip();
            protocolEncoderOutput.write(buffer);

        }

    }

}
