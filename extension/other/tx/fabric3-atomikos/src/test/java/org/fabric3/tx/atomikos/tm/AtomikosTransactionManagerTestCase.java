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
package org.fabric3.tx.atomikos.tm;

import javax.transaction.Status;
import javax.transaction.Transaction;
import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.RuntimeRecover;

/**
 *
 */
public class AtomikosTransactionManagerTestCase extends TestCase {
    private EventService eventService;
    private HostInfo info;
    private File dataDir;

    public void testTransactionManagerInit() throws Exception {
        EasyMock.expect(info.getDataDir()).andReturn(dataDir);
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");

        EasyMock.replay(eventService, info);

        AtomikosTransactionManager tm = new AtomikosTransactionManager(eventService, info);

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

        EasyMock.verify(eventService, info);
    }

    public void testTransactionManagerProperties() throws Exception {
        EasyMock.expect(info.getDataDir()).andReturn(dataDir);
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");

        EasyMock.replay(eventService, info);

        AtomikosTransactionManager tm = new AtomikosTransactionManager(eventService, info);

        tm.setTimeout(10000);
        tm.setCheckPointInterval(10000);
        tm.setEnableLogging(false);
        tm.setSingleThreaded2PC(true);
        tm.init();
        tm.onEvent(new RuntimeRecover());

        tm.destroy();

        EasyMock.verify(eventService, info);
    }

    @Override
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    protected void setUp() throws Exception {
        super.setUp();
        dataDir = new File("transactionManagertestcase");
        dataDir.mkdir();

        eventService = EasyMock.createNiceMock(EventService.class);
        info = EasyMock.createMock(HostInfo.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.forceDelete(dataDir);
    }
}
