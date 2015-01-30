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
package org.fabric3.databinding.json.transform;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.transform.Transformer;

/**
 * Transforms a Java object to a byte array.
 */
public class Object2BytesJsonTransformer implements Transformer<Object, byte[]> {
    private ObjectMapper mapper;

    public Object2BytesJsonTransformer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public byte[] transform(Object source, ClassLoader loader) throws ContainerException {
        try {
            return mapper.writeValueAsBytes(source);
        } catch (IOException e) {
            throw new ContainerException(e);
        }
    }

}