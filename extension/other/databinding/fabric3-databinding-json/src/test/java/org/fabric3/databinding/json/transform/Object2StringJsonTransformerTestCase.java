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

import junit.framework.TestCase;

/**
 *
 */
public class Object2StringJsonTransformerTestCase extends TestCase {

    public void testTransform() throws Exception {
        Object2StringJsonTransformerFactory factory = new Object2StringJsonTransformerFactory();
        Object2StringJsonTransformer transformer = factory.create(null, null, null, null);
        Foo foo = new Foo();
        foo.setBar("bar");
        String result = transformer.transform(foo, getClass().getClassLoader());
        assertEquals("{\"bar\":\"bar\"}",result);
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