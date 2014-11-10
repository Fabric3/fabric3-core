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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 *
 */
public class CallbackReferenceSerializerTestCase extends TestCase {

    public void testSerializeToString() throws Exception {
        List<CallbackReference> references = new ArrayList<>();
        CallbackReference reference1 = new CallbackReference("uri1", "correlation1");
        CallbackReference reference2 = new CallbackReference("uri2", "correlation2");
        references.add(reference1);
        references.add(reference2);
        String serialized = CallbackReferenceSerializer.serializeToString(references);

        List<CallbackReference> result = CallbackReferenceSerializer.deserialize(serialized);

        assertEquals(2, result.size());
        CallbackReference resultReference1 = result.get(0);
        CallbackReference resultFrame2 = result.get(1);
        assertEquals("uri1", resultReference1.getServiceUri());
        assertEquals("correlation1", resultReference1.getCorrelationId());
        assertEquals("uri2", resultFrame2.getServiceUri());
        assertEquals("correlation2", resultFrame2.getCorrelationId());
    }

    public void testSerializeToStringNoCorrelationId() throws Exception {
        List<CallbackReference> references = new ArrayList<>();
        CallbackReference reference1 = new CallbackReference("uri1", null);
        CallbackReference reference2 = new CallbackReference("uri2", null);
        references.add(reference1);
        references.add(reference2);
        String serialized = CallbackReferenceSerializer.serializeToString(references);

        List<CallbackReference> result = CallbackReferenceSerializer.deserialize(serialized);

        assertEquals(2, result.size());
        CallbackReference resultReference1 = result.get(0);
        CallbackReference resultFrame2 = result.get(1);
        assertEquals("uri1", resultReference1.getServiceUri());
        assertNull(resultReference1.getCorrelationId());
        assertEquals("uri2", resultFrame2.getServiceUri());
        assertNull(resultFrame2.getCorrelationId());
    }

    public void testSerializeToBytes() throws Exception {
        List<CallbackReference> references = new ArrayList<>();
        CallbackReference reference1 = new CallbackReference("uri1", "correlation1");
        CallbackReference reference2 = new CallbackReference("uri2", "correlation2");
        references.add(reference1);
        references.add(reference2);
        byte[] serialized = CallbackReferenceSerializer.serializeToBytes(references);

        List<CallbackReference> result = CallbackReferenceSerializer.deserialize(serialized);

        assertEquals(2, result.size());
        CallbackReference resultReference1 = result.get(0);
        CallbackReference resultFrame2 = result.get(1);
        assertEquals("uri1", resultReference1.getServiceUri());
        assertEquals("correlation1", resultReference1.getCorrelationId());
        assertEquals("uri2", resultFrame2.getServiceUri());
        assertEquals("correlation2", resultFrame2.getCorrelationId());

    }

    public void testSerializeToBytesNoCorrelationId() throws Exception {
        List<CallbackReference> references = new ArrayList<>();
        CallbackReference reference1 = new CallbackReference("uri1", null);
        CallbackReference reference2 = new CallbackReference("uri2",null);
        references.add(reference1);
        references.add(reference2);
        byte[] serialized = CallbackReferenceSerializer.serializeToBytes(references);

        List<CallbackReference> result = CallbackReferenceSerializer.deserialize(serialized);

        assertEquals(2, result.size());
        CallbackReference resultReference1 = result.get(0);
        CallbackReference resultFrame2 = result.get(1);
        assertEquals("uri1", resultReference1.getServiceUri());
        assertNull(resultReference1.getCorrelationId());
        assertEquals("uri2", resultFrame2.getServiceUri());
        assertNull(resultFrame2.getCorrelationId());

    }

}
