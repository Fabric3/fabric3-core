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
package org.fabric3.spi.container.invocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializes and (De)Serializes a {@link CallbackReference} to a byte array or String.
 */
public class CallbackReferenceSerializer {

    public static String serializeToString(List<CallbackReference> references) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (CallbackReference reference : references) {
            String correlationId = reference.getCorrelationId();
            if (correlationId == null) {
                builder.append(",");
            } else {
                builder.append(correlationId).append(",");
            }
            String callbackUri = reference.getServiceUri();
            builder.append(callbackUri).append(",");
        }
        return builder.toString();
    }

    public static byte[] serializeToBytes(List<CallbackReference> references) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream das = new DataOutputStream(bas);
        das.writeInt(references.size());
        for (CallbackReference reference : references) {
            String correlationId = reference.getCorrelationId();
            if (correlationId == null) {
                das.writeInt(0);
            } else {
                das.writeInt(correlationId.length());
                das.writeBytes(correlationId);
            }
            String callbackUri = reference.getServiceUri();
            das.writeInt(callbackUri.length());
            das.writeBytes(callbackUri);
        }
        return bas.toByteArray();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static List<CallbackReference> deserialize(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        int number = dis.readInt();
        List<CallbackReference> references = new ArrayList<CallbackReference>(number);
        while (number > 0) {
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
            references.add(new CallbackReference(callbackUri, correlationId));
            number--;
        }
        return references;
    }

    public static List<CallbackReference> deserialize(String serialized) throws IOException {
        List<CallbackReference> references = new ArrayList<CallbackReference>();
        String[] tokens = serialized.split(",");
        for (int i = 0; i < tokens.length; i = i + 2) {
            String callbackUri = tokens[i + 1];
            String correlationId = tokens[i].length() == 0 ? null : tokens[i];
            references.add(new CallbackReference(callbackUri, correlationId));
        }

        return references;
    }

}
