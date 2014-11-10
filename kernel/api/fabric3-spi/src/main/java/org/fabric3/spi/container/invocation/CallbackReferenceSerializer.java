/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    public static String serializeToString(List<CallbackReference> references){
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
        List<CallbackReference> references = new ArrayList<>(number);
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

    public static List<CallbackReference> deserialize(String serialized) {
        List<CallbackReference> references = new ArrayList<>();
        String[] tokens = serialized.split(",");
        for (int i = 0; i < tokens.length; i = i + 2) {
            String callbackUri = tokens[i + 1];
            String correlationId = tokens[i].length() == 0 ? null : tokens[i];
            references.add(new CallbackReference(callbackUri, correlationId));
        }

        return references;
    }

}
