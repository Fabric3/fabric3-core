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
package org.fabric3.fabric.container.builder.transform;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.container.interceptor.TransformerInterceptor;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.container.wire.Interceptor;

/**
 *
 */
public class TransformerInterceptorTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testInvoke() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        MessageImpl message = new MessageImpl();
        message.setBody(new Object[]{"test"});

        Transformer<Object, Object> in = EasyMock.createMock(Transformer.class);
        Transformer<Object, Object> out = EasyMock.createMock(Transformer.class);
        EasyMock.expect(in.transform(EasyMock.notNull(), EasyMock.eq(loader))).andReturn("in");
        EasyMock.expect(out.transform(EasyMock.notNull(), EasyMock.eq(loader))).andReturn("out");
        Interceptor next = EasyMock.createMock(Interceptor.class);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(message);
        EasyMock.replay(in, out, next);

        TransformerInterceptor interceptor = new TransformerInterceptor(in, out, loader, loader);
        interceptor.setNext(next);
        interceptor.invoke(message);

        EasyMock.verify(in, out, next);
    }
}
