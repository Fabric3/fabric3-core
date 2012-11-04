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
package org.fabric3.tx.atomikos.tm;

import java.io.File;
import javax.transaction.Status;
import javax.transaction.Transaction;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.MonitorChannel;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.util.FileHelper;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.RuntimeRecover;

/**
 *
 */
public class AtomikosTransactionManagerTestCase extends TestCase {
    private EventService eventService;
    private HostInfo info;
    private MonitorChannel channel;
    private File dataDir;

    public void testTransactionManagerInit() throws Exception {
        EasyMock.expect(info.getDataDir()).andReturn(dataDir);
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");

        EasyMock.replay(eventService, info, channel);

        AtomikosTransactionManager tm = new AtomikosTransactionManager(eventService, info, channel);

        tm.init();
        tm.onEvent(new RuntimeRecover());

        tm.setTimeout(10000);
        tm.begin();
        assertNotNull(tm.getTransaction());
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        Transaction trx = tm.suspend();
        tm.resume(trx);
        tm.commit();

        tm.begin();
        tm.setRollbackOnly();
        tm.rollback();

        tm.destroy();

        EasyMock.verify(eventService, info, channel);
    }

    public void testTransactionManagerProperties() throws Exception {
        EasyMock.expect(info.getDataDir()).andReturn(dataDir);
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");

        EasyMock.replay(eventService, info, channel);

        AtomikosTransactionManager tm = new AtomikosTransactionManager(eventService, info, channel);

        tm.setTimeout(10000);
        tm.setCheckPointInterval(10000);
        tm.setEnableLogging(false);
        tm.setSingleThreaded2PC(true);
        tm.init();
        tm.onEvent(new RuntimeRecover());

        tm.destroy();

        EasyMock.verify(eventService, info, channel);
    }

    @Override
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void setUp() throws Exception {
        super.setUp();
        dataDir = new File("transactionManagertestcase");
        dataDir.mkdir();

        eventService = EasyMock.createNiceMock(EventService.class);
        channel = EasyMock.createNiceMock(MonitorChannel.class);
        info = EasyMock.createMock(HostInfo.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.forceDelete(dataDir);
    }
}
