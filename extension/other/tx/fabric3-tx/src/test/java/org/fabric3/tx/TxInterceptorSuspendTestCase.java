/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
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
        Message fault = new MessageImpl("", true, null);
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
