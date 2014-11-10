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
package org.fabric3.tx;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.wire.Interceptor;

/**
 *
 */
public class TxInterceptorSuspendTestCase extends TestCase {
    private TransactionManager tm;
    private TxInterceptor interceptor;
    private Interceptor next;
    private MessageImpl message;

    public void testSuspend() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(message);
        EasyMock.expect(tm.suspend()).andReturn(trx);
        tm.resume(trx);
        EasyMock.replay(tm, next);

        interceptor.invoke(message);

        EasyMock.verify(tm, next);
    }

    public void testSuspendOnError() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andThrow(new MockException());
        EasyMock.expect(tm.suspend()).andReturn(trx);
        tm.resume(trx);
        EasyMock.replay(tm, next);

        try {
            interceptor.invoke(message);
            fail();
        } catch (MockException e) {
            // expected
        }

        EasyMock.verify(tm, next);
    }

    public void testSuspendOnFault() throws Exception {
        Message fault = new MessageImpl();
        message.setBodyWithFault("");
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(fault);
        EasyMock.expect(tm.suspend()).andReturn(trx);
        tm.resume(trx);
        EasyMock.replay(tm, next);

        interceptor.invoke(message);

        EasyMock.verify(tm, next);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tm = EasyMock.createMock(TransactionManager.class);
        TxMonitor monitor = EasyMock.createNiceMock(TxMonitor.class);
        EasyMock.replay(monitor);
        interceptor = new TxInterceptor(tm, TxAction.SUSPEND, monitor);
        message = new MessageImpl();
        next = EasyMock.createMock(Interceptor.class);
        interceptor.setNext(next);
    }

    private class MockException extends RuntimeException {

        private static final long serialVersionUID = -1746683997765583218L;
    }


}
