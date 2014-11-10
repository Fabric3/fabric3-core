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

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.cfg.Annotations;
import com.fasterxml.jackson.jaxrs.json.JsonMapperConfigurator;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.transform.TransformerFactory;

/**
 * Creates Transformers capable of marshalling serialized XML Strings to Java types using JSON.
 */
public class String2ObjectJsonTransformerFactory implements TransformerFactory {
    private final static Annotations[] DEFAULT_ANNOTATIONS = {Annotations.JACKSON, Annotations.JAXB};
    private JsonMapperConfigurator configurator;

    public String2ObjectJsonTransformerFactory() {
        configurator = new JsonMapperConfigurator(null, DEFAULT_ANNOTATIONS);
    }

    public int getOrder() {
        return 0;
    }

    public boolean canTransform(DataType source, DataType target) {
        return source instanceof JsonType && String.class.equals(source.getType()) && target instanceof JavaType;
    }

    public String2ObjectJsonTransformer create(DataType source, DataType target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes) {
        JavaType type = (JavaType) target;
        ObjectMapper mapper = configurator.getDefaultMapper();
        Class clazz = type.getType();
        return new String2ObjectJsonTransformer(clazz, mapper);
    }


}