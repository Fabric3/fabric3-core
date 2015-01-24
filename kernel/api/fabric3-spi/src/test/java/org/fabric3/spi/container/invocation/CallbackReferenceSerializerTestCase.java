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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 *
 */
public class CallbackReferenceSerializerTestCase extends TestCase {

    public void testSerializeToString() throws Exception {
        List<String> references = Arrays.asList("uri1", "uri2");
        String serialized = CallbackReferenceSerializer.serializeToString(references);

        List<String> result = CallbackReferenceSerializer.deserialize(serialized);

        assertEquals(2, result.size());
        assertEquals("uri1", result.get(0));
        assertEquals("uri2", result.get(1));
    }

    public void testSerializeToBytes() throws Exception {
        List<String> references = Arrays.asList("uri1", "uri2");
        byte[] serialized = CallbackReferenceSerializer.serializeToBytes(references);

        List<String> result = CallbackReferenceSerializer.deserialize(serialized);

        assertEquals(2, result.size());
        assertEquals("uri1", result.get(0));
        assertEquals("uri2", result.get(1));

    }

}
