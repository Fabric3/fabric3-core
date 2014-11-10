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
package org.fabric3.spi.classloader;

import java.io.IOException;
import java.io.Serializable;

/**
 * Serializes and deserializes objects. This service provides the additional capability of deserializing the object (or object graph) in the context
 * of the runtime classloader network.
 */
public interface SerializationService {

    /**
     * Serialize an object.
     *
     * @param serializable the object
     * @return the serialized byte array
     * @throws IOException if there is an error during serialization
     */
    byte[] serialize(Serializable serializable) throws IOException;

    /**
     * Deserialize an object.
     *
     * @param type  the expected type
     * @param bytes the serialized byte array
     * @return an object instance
     * @throws IOException            if there is an error during serialization
     * @throws ClassNotFoundException if the class cannot be loaded
     */
    <T> T deserialize(Class<T> type, byte[] bytes) throws IOException, ClassNotFoundException;

}
