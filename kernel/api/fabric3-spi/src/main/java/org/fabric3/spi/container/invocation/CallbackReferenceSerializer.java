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
import java.util.Collections;
import java.util.List;

/**
 * Serializes and (De)Serializes a callback reference to a byte array or String.
 */
public class CallbackReferenceSerializer {

    public static String serializeToString(List<String> references) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String reference : references) {
            if (first) {
                builder.append(reference);
                first = false;
            } else {
                builder.append(",").append(reference);
            }
        }
        return builder.toString();
    }

    public static byte[] serializeToBytes(List<String> references) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        DataOutputStream das = new DataOutputStream(bas);
        das.writeInt(references.size());
        for (String reference : references) {
            das.writeInt(reference.length());
            das.write(reference.getBytes());
        }
        return bas.toByteArray();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static List<String> deserialize(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);
        int number = dis.readInt();
        List<String> references = new ArrayList<>(number);
        while (number > 0) {
            String callbackReference = null;
            int callbackUriSize = dis.readInt();
            if (callbackUriSize > 0) {
                byte[] uriBytes = new byte[callbackUriSize];
                dis.read(uriBytes);
                callbackReference = new String(uriBytes);
            }
            references.add(callbackReference);
            number--;
        }
        return references;
    }

    public static List<String> deserialize(String serialized) {
        List<String> references = new ArrayList<>();
        String[] tokens = serialized.split(",");
        Collections.addAll(references, tokens);
        return references;
    }

}
