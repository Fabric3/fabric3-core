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

import javax.transaction.Status;
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
public class TxInterceptorTestCase extends TestCase {
    private TransactionManager tm;
    private Interceptor next;
    private MessageImpl message;
    private TxMonitor monitor;

    public void testBeginCommit() throws Exception {
        EasyMock.expect(tm.getTransaction()).andReturn(null);
        EasyMock.expect(tm.getStatus()).andReturn(Status.STATUS_ACTIVE);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(message);
        tm.begin();
        tm.commit();
        EasyMock.replay(tm, next);

        TxInterceptor interceptor = new TxInterceptor(tm, TxAction.BEGIN, monitor);
        interceptor.setNext(next);

        interceptor.invoke(message);

        EasyMock.verify(tm, next);
    }

    public void testAlreadyBegunNoCommit() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(message);
        EasyMock.replay(trx, tm, next);

        TxInterceptor interceptor = new TxInterceptor(tm, TxAction.BEGIN, monitor);
        interceptor.setNext(next);

        interceptor.invoke(message);

        EasyMock.verify(trx, tm, next);
    }

    public void testRollbackOnThrownException() throws Exception {
        EasyMock.expect(tm.getTransaction()).andReturn(null);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andThrow(new MockException());
        tm.begin();
        tm.rollback();
        EasyMock.replay(tm, next);

        try {
            TxInterceptor interceptor = new TxInterceptor(tm, TxAction.BEGIN, monitor);
            interceptor.setNext(next);

            interceptor.invoke(message);
            fail();
        } catch (MockException e) {
            // expected
        }

        EasyMock.verify(tm, next);
    }

    public void testRollbackOnFault() throws Exception {
        Message fault = new MessageImpl();
        fault.setBodyWithFault("");
        EasyMock.expect(tm.getTransaction()).andReturn(null);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(fault);
        tm.begin();
        tm.rollback();
        EasyMock.replay(tm, next);

        TxInterceptor interceptor = new TxInterceptor(tm, TxAction.BEGIN, monitor);
        interceptor.setNext(next);

        interceptor.invoke(message);

        EasyMock.verify(tm, next);
    }

    public void testNoRollbackOnThrownExceptionWhenNotBegun() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andThrow(new MockException());
        EasyMock.replay(trx, tm, next);

        try {
            TxInterceptor interceptor = new TxInterceptor(tm, TxAction.BEGIN, monitor);
            interceptor.setNext(next);

            interceptor.invoke(message);
            fail();
        } catch (MockException e) {
            // expected
        }

        EasyMock.verify(trx, tm, next);
    }

    public void testNoRollbackOnFaultWhenNotBegun() throws Exception {
        Message fault = new MessageImpl();
        fault.setBodyWithFault("");
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(fault);
        EasyMock.replay(tm, next);

        TxInterceptor interceptor = new TxInterceptor(tm, TxAction.BEGIN, monitor);
        interceptor.setNext(next);

        interceptor.invoke(message);

        EasyMock.verify(tm, next);
    }

    public void testPropagate() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);

        TxInterceptor interceptor = new TxInterceptor(tm, TxAction.PROPAGATE, monitor);
        interceptor.setNext(next);
        EasyMock.expect(next.invoke(EasyMock.isA(Message.class))).andReturn(message);
        EasyMock.replay(tm, next);

        interceptor.invoke(message);

        EasyMock.verify(tm, next);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tm = EasyMock.createMock(TransactionManager.class);
        monitor = EasyMock.createNiceMock(TxMonitor.class);
        EasyMock.replay(monitor);
        message = new MessageImpl();
        next = EasyMock.createMock(Interceptor.class);
    }

    private class MockException extends RuntimeException {

        private static final long serialVersionUID = -1746683997765583218L;
    }


}
