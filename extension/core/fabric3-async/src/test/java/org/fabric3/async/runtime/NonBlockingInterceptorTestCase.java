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
package org.fabric3.async.runtime;

import java.util.Collections;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.spi.invocation.CallbackReference;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;
import org.fabric3.spi.wire.Interceptor;

/**
 *
 */
public class NonBlockingInterceptorTestCase extends TestCase {
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
        CallbackReference callbackReference = new CallbackReference(null, null);
        workContext.addCallbackReference(callbackReference);
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
