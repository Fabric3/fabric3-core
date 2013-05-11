/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.spi.invocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and (De)Serializes a {@link CallFrame} to a byte array or String.
 */
public class CallFrameSerializer {

    public static String serializeToString(List<CallFrame> frames) throws IOException {
        StringBuilder builder = new StringBuilder();
        //builder.append(frames.size());
        for (CallFrame frame : frames) {
            String correlationId = frame.getCorrelationId();
            if (correlationId == null) {
                builder.append(",");
            } else {
                builder.append(correlationId).append(",");
            }
            String callbackUri = frame.getCallbackUri();
            builder.append(callbackUri).append(",");
        }
        return builder.toString();
    }

    public static byte[] serializeToBytes(List<CallFrame> frames) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream das = new DataOutputStream(bas);
        das.writeInt(frames.size());
        for (CallFrame frame : frames) {
            String correlationId = frame.getCorrelationId();
            if (correlationId == null) {
                das.writeInt(0);
            } else {
                das.writeInt(correlationId.length());
                das.writeBytes(correlationId);
            }
            String callbackUri = frame.getCallbackUri();
            das.writeInt(callbackUri.length());
            das.writeBytes(callbackUri);
        }
        return bas.toByteArray();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static List<CallFrame> deserialize(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        int numberOfFrames = dis.readInt();
        List<CallFrame> frames = new ArrayList<CallFrame>(numberOfFrames);
        while (numberOfFrames > 0) {
            String correlationId = null;
            int correlationSize = dis.readInt();
            if (correlationSize > 0) {
                byte[] correlationBytes = new byte[correlationSize];
                dis.read(correlationBytes);
                correlationId = new String(correlationBytes);
            }
            String callbackUri = null;
            int callbackUriSize = dis.readInt();
            if (callbackUriSize > 0) {
                byte[] uriBytes = new byte[callbackUriSize];
                dis.read(uriBytes);
                callbackUri = new String(uriBytes);
            }
            frames.add(new CallFrame(callbackUri, correlationId));
            numberOfFrames--;
        }
        return frames;
    }

    public static List<CallFrame> deserialize(String serialized) throws IOException {
        List<CallFrame> frames = new ArrayList<CallFrame>();
        String[] tokens = serialized.split(",");
        for (int i = 0; i < tokens.length; i = i + 2) {
            String callbackUri = tokens[i + 1];
            String correlationId = tokens[i].length() == 0 ? null : tokens[i];
            frames.add(new CallFrame(callbackUri, correlationId));
        }

        return frames;
    }

}
