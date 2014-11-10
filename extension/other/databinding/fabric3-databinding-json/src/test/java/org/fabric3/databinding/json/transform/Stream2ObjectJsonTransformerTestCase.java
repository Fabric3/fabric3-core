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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class Stream2ObjectJsonTransformerTestCase extends TestCase {

    public void testTransform() throws Exception {
        Stream2ObjectJsonTransformerFactory factory = new Stream2ObjectJsonTransformerFactory();
        JavaType javaType = new JavaType(Foo.class);
        Stream2ObjectJsonTransformer transformer = factory.create(null, javaType, null, null);
        String text = "{\"bar\":\"bar\"}";
        InputStream stream = new ByteArrayInputStream(text.getBytes());
        Foo result = (Foo) transformer.transform(stream, getClass().getClassLoader());
        assertEquals("bar", result.getBar());
    }

    public static class Foo {
        private String bar;

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }
    }

}