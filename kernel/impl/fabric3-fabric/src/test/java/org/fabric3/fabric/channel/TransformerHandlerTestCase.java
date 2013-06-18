/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.channel;

import java.io.Serializable;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.EventWrapper;
import org.fabric3.spi.model.type.java.JavaClass;
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
        DataType<?> sourceType = new JsonType<String>(String.class, "{test}");
        DataType<?> targetType = new JavaClass<String>(String.class);
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

        TransformerHandler handler = new TransformerHandler((DataType<Object>) targetType, registry);
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
        DataType<?> dataType = new JavaClass<String>(String.class);
        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);
        next.handle(EasyMock.notNull(), EasyMock.anyBoolean());

        EasyMock.replay(registry, next);

        TransformerHandler handler = new TransformerHandler((DataType<Object>) dataType, registry);
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