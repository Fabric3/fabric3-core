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
package org.fabric3.tx;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.channel.EventStreamHandler;

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
        next.handle(EasyMock.isA(Object.class));
        tm.begin();
        tm.commit();
        EasyMock.replay(tm, next);

        handler.handle(event);

        EasyMock.verify(tm, next);
    }

    public void testAlreadyBegunNoCommit() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        next.handle(EasyMock.isA(Object.class));
        EasyMock.replay(trx, tm, next);

        handler.handle(event);

        EasyMock.verify(trx, tm, next);
    }

    public void testRollbackOnThrownException() throws Exception {
        EasyMock.expect(tm.getTransaction()).andReturn(null);
        next.handle(EasyMock.isA(Object.class));
        EasyMock.expectLastCall().andThrow(new MockException());
        tm.begin();
        tm.rollback();
        EasyMock.replay(tm, next);

        try {
            handler.handle(event);
            fail();
        } catch (MockException e) {
            // expected
        }

        EasyMock.verify(tm, next);
    }


    public void testNoRollbackOnThrownExceptionWhenNotBegun() throws Exception {
        Transaction trx = EasyMock.createMock(Transaction.class);
        EasyMock.expect(tm.getTransaction()).andReturn(trx);
        next.handle(EasyMock.isA(Object.class));
        EasyMock.expectLastCall().andThrow(new MockException());
        EasyMock.replay(trx, tm, next);

        try {
            handler.handle(event);
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
