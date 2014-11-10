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

import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 *
 */
public class TxEventStreamHandlerTestCase extends TestCase {
    private TransactionManager tm;
    private TxEventStreamHandler handler;
    private EventStreamHandler next;
    private Object event;

    public void testBeginCommit() throws Exception {
        EasyMock.expect(tm.getTransaction()).andReturn(null);
        EasyMock.expect(tm.getStatus()).andReturn(Status.STATUS_ACTIVE);
        next.handle(EasyMock.isA(Object.class), EasyMock.anyBoolean());
        tm.begin();
        tm.commit();
        EasyMock.replay(tm, next);

        handler.handle(event, true);

        EasyMock.verify(tm, next);
    }

    public void testAlreadyBegunNoCommit() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        next.handle(EasyMock.isA(Object.class), EasyMock.anyBoolean());
        EasyMock.replay(trx, tm, next);

        handler.handle(event, true);

        EasyMock.verify(trx, tm, next);
    }

    public void testRollbackOnThrownException() throws Exception {
        EasyMock.expect(tm.getTransaction()).andReturn(null);
        next.handle(EasyMock.isA(Object.class), EasyMock.anyBoolean());
        EasyMock.expectLastCall().andThrow(new MockException());
        tm.begin();
        tm.rollback();
        EasyMock.replay(tm, next);

        try {
            handler.handle(event, true);
            fail();
        } catch (MockException e) {
            // expected
        }

        EasyMock.verify(tm, next);
    }

    public void testNoRollbackOnThrownExceptionWhenNotBegun() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        next.handle(EasyMock.isA(Object.class), EasyMock.anyBoolean());
        EasyMock.expectLastCall().andThrow(new MockException());
        EasyMock.replay(trx, tm, next);

        try {
            handler.handle(event, true);
            fail();
        } catch (MockException e) {
            // expected
        }

        EasyMock.verify(trx, tm, next);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tm = EasyMock.createMock(TransactionManager.class);
        TxMonitor monitor = EasyMock.createNiceMock(TxMonitor.class);
        EasyMock.replay(monitor);
        handler = new TxEventStreamHandler(tm, TxAction.BEGIN, monitor);
        event = new Object();
        next = EasyMock.createMock(EventStreamHandler.class);
        handler.setNext(next);
    }

    private class MockException extends RuntimeException {

        private static final long serialVersionUID = -1746683997765583218L;
    }

}
