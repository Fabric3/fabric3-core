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
package org.fabric3.management.rest.transformer;

import java.io.InputStream;

import org.fabric3.spi.transform.Transformer;

/**
 * Transformers used to (de)serialize a request/response types.
 */
public class TransformerPair {
    private Transformer<InputStream, Object> deserializer;
    private Transformer<Object, byte[]> serializer;

    /**
     * Constructor.
     *
     * @param deserializer the transformer used to deserialize request types
     * @param serializer   the transformer used to serialize response types
     */
    public TransformerPair(Transformer<InputStream, Object> deserializer, Transformer<Object, byte[]> serializer) {
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    /**
     * Returns the transformer used to deserialize request types.
     *
     * @return the transformer used to deserialize request types
     */
    public Transformer<InputStream, Object> getDeserializer() {
        return deserializer;
    }

    /**
     * Returns the transformer used to serialize response types.
     *
     * @return the transformer used to serialize response types
     */
    public Transformer<Object, byte[]> getSerializer() {
        return serializer;
    }
}
