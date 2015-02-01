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
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.transform.Transformer;

/**
 * Transforms a Java object to a serialized JSON String.
 */
public class Object2StringJsonTransformer implements Transformer<Object, String> {
    private ObjectMapper mapper;

    public Object2StringJsonTransformer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String transform(Object source, ClassLoader loader) throws Fabric3Exception {
        try {
            return mapper.writeValueAsString(source);
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

}