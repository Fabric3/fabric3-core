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
package org.fabric3.binding.web.runtime.channel;

import java.io.Serializable;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
public class TransformerHandlerTestCase extends TestCase {
    private EventStreamHandler next;

    @SuppressWarnings({"unchecked"})
    public void testTransform() throws Exception {
        DataType sourceType = new JsonType(String.class);
        JavaType targetType = new JavaType(String.class);
        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        Transformer transformer = EasyMock.createMock(Transformer.class);

        // the transformer should only be created once and called twice since it will be cached after the first invocation
        EasyMock.expect(registry.getTransformer(EasyMock.isA(DataType.class),
                                                EasyMock.isA(DataType.class),
                                                EasyMock.isA(List.class),
                                                EasyMock.isA(List.class))).andReturn(transformer);
        EasyMock.expect(transformer.transform(EasyMock.isA(MockEvent.class), EasyMock.isA(ClassLoader.class))).andReturn(new Object()).times(2);

        next.handle(EasyMock.notNull(), EasyMock.anyBoolean());
        EasyMock.expectLastCall().times(2);

        EasyMock.replay(registry, next, transformer);

        TransformerHandler handler = new TransformerHandler(targetType, registry);
        handler.setNext(next);

        Object event = new MockEvent();
        EventWrapper wrapper = new EventWrapper(sourceType, event);
        handler.handle(wrapper, true);

        EventWrapper wrapper2 = new EventWrapper(sourceType, event);
        handler.handle(wrapper2, true);

        EasyMock.verify(registry, next, transformer);
    }

    @SuppressWarnings({"unchecked"})
    public void testNoTransform() throws Exception {
        DataType dataType = new JavaType(String.class);
        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        next.handle(EasyMock.notNull(), EasyMock.anyBoolean());

        EasyMock.replay(registry, next);

        TransformerHandler handler = new TransformerHandler(dataType, registry);
        handler.setNext(next);

        handler.handle(new Object(), true);

        EasyMock.verify(registry, next);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        next = EasyMock.createMock(EventStreamHandler.class);
    }

    private class MockEvent implements Serializable {
        private static final long serialVersionUID = 3194806347865372031L;
    }
}