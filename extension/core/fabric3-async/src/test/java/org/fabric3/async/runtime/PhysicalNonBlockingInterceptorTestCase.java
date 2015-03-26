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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.async.runtime;

import java.util.Collections;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;

/**
 *
 */
public class PhysicalNonBlockingInterceptorTestCase extends TestCase {
    private Interceptor next;
    private NonBlockingInterceptor interceptor;
    private ExecutorService executorService;
    private WorkContext workContext;

    public void testInvoke() throws Exception {
        final Message message = new MessageImpl();
        message.setWorkContext(workContext);
        executorService.execute(EasyMock.isA(AsyncRequest.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                AsyncRequest request = (AsyncRequest) EasyMock.getCurrentArguments()[0];
                request.run();
                return null;
            }
        });
        EasyMock.replay(executorService);
        assertNotNull(interceptor.invoke(message));

    }

    public void testNextInterceptor() {
        assertSame(next, interceptor.getNext());
    }

    protected void setUp() throws Exception {
        super.setUp();
        workContext = new WorkContext();
        workContext.addHeaders(Collections.<String, Object>singletonMap("key", "value"));

        executorService = EasyMock.createMock(ExecutorService.class);
        next = new MockInterceptor(workContext);
        NonBlockingMonitor monitor = EasyMock.createNiceMock(NonBlockingMonitor.class);
        interceptor = new NonBlockingInterceptor(executorService, monitor);
        interceptor.setNext(next);
    }

    private class MockInterceptor implements Interceptor {
        private WorkContext originalWorkContext;

        public MockInterceptor(WorkContext workContext) {
            originalWorkContext = workContext;
        }

        public Message invoke(Message msg) {
            WorkContext workContext = WorkContextCache.getThreadWorkContext();
            assertNotSame(originalWorkContext, workContext);
            assertFalse(workContext.getHeaders().isEmpty());
            msg.reset();
            return msg;
        }

        public void setNext(Interceptor next) {
        }

        public Interceptor getNext() {
            return null;
        }
    }
}
