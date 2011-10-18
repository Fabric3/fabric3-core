/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.file.runtime.receiver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.file.api.ServiceAdapter;
import org.fabric3.binding.file.api.InvalidDataException;
import org.fabric3.binding.file.common.Strategy;
import org.fabric3.host.util.FileHelper;
import org.fabric3.host.util.IOHelper;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class FileSystemReceiverTestCase extends TestCase {
    private static final String DEFAULT_HEADER = "header123.xml";
    private File location = new File("drop");
    private File errorDirectory = new File("errorDirectory");
    private File archiveDirectory = new File("archiveDirectory");
    private ReceiverMonitor monitor;
    private Interceptor interceptor;
    private ServiceAdapter adapter;

    public void testFileReceivedAndDeleted() throws Exception {
        FileSystemReceiver receiver = createReceiver("header.*\\.xml", Strategy.DELETE);

        File file = new File(location, DEFAULT_HEADER);
        EasyMock.expect(adapter.beforeInvoke(EasyMock.eq(file))).andReturn(new Object[]{});
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andReturn(new MessageImpl());
        adapter.afterInvoke(EasyMock.eq(file), EasyMock.isA(Object[].class));
        adapter.delete(EasyMock.eq(file));

        EasyMock.replay(adapter, interceptor);
        createFile(DEFAULT_HEADER);
        receiver.run();    // invoke twice because the file entry is cached on the first run
        receiver.run();
        EasyMock.verify(adapter, interceptor);
    }

    public void testFileReceivedAndArchived() throws Exception {
        FileSystemReceiver receiver = createReceiver("header.*\\.xml", Strategy.ARCHIVE);

        File file = new File(location, DEFAULT_HEADER);
        EasyMock.expect(adapter.beforeInvoke(EasyMock.eq(file))).andReturn(new Object[]{});
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andReturn(new MessageImpl());
        adapter.afterInvoke(EasyMock.eq(file), EasyMock.isA(Object[].class));
        adapter.archive(EasyMock.eq(file), EasyMock.eq(archiveDirectory));

        EasyMock.replay(adapter, interceptor);
        createFile(DEFAULT_HEADER);
        receiver.run();    // invoke twice because the file entry is cached on the first run
        receiver.run();
        EasyMock.verify(adapter, interceptor);
    }

    public void testFileIgnore() throws Exception {
        FileSystemReceiver receiver = createReceiver("header.*\\.xml", Strategy.DELETE);
        EasyMock.replay(adapter, interceptor);
        createFile("test.xml");
        receiver.run();    // invoke twice because the file entry is cached on the first run
        receiver.run();
        EasyMock.verify(adapter, interceptor);
    }

    public void testInvalidFile() throws Exception {
        FileSystemReceiver receiver = createReceiver("header.*\\.xml", Strategy.DELETE);

        File file = new File(location, DEFAULT_HEADER);
        InvalidDataException exception = new InvalidDataException("test");
        EasyMock.expect(adapter.beforeInvoke(EasyMock.eq(file))).andThrow(exception);
        adapter.error(EasyMock.eq(file), EasyMock.eq(errorDirectory), EasyMock.eq(exception));

        EasyMock.replay(adapter, interceptor);
        createFile(DEFAULT_HEADER);
        receiver.run();    // invoke twice because the file entry is cached on the first run
        receiver.run();
        EasyMock.verify(adapter, interceptor);
    }

    public void testServiceRuntimeExceptionOnInvoke() throws Exception {
        FileSystemReceiver receiver = createReceiver("header.*\\.xml", Strategy.DELETE);

        File file = new File(location, DEFAULT_HEADER);
        EasyMock.expect(adapter.beforeInvoke(EasyMock.eq(file))).andReturn(new Object[]{}).times(2);
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andThrow(new ServiceRuntimeException("test"));
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andReturn(new MessageImpl());
        adapter.afterInvoke(EasyMock.eq(file), EasyMock.isA(Object[].class));
        EasyMock.expectLastCall().times(2);
        adapter.delete(EasyMock.eq(file));
        EasyMock.replay(adapter, interceptor);
        createFile(DEFAULT_HEADER);
        receiver.run();    // invoke twice because the file entry is cached on the first run
        receiver.run();

        // now verify a correct run
        receiver.run();    // invoke twice because the file entry is re-cached on the first run
        receiver.run();

        EasyMock.verify(adapter, interceptor);
    }

    public void testServiceCheckedExceptionOnInvoke() throws Exception {
        FileSystemReceiver receiver = createReceiver("header.*\\.xml", Strategy.DELETE);

        File file = new File(location, DEFAULT_HEADER);
        EasyMock.expect(adapter.beforeInvoke(EasyMock.eq(file))).andReturn(new Object[]{});
        MessageImpl errorMessage = new MessageImpl();
        Exception fault = new Exception();
        errorMessage.setBodyWithFault(fault);
        EasyMock.expect(interceptor.invoke(EasyMock.isA(Message.class))).andReturn(errorMessage);
        adapter.afterInvoke(EasyMock.eq(file), EasyMock.isA(Object[].class));
        EasyMock.expectLastCall();
        adapter.error(EasyMock.eq(file), EasyMock.eq(errorDirectory), EasyMock.eq(fault));
        EasyMock.replay(adapter, interceptor);
        createFile(DEFAULT_HEADER);
        receiver.run();    // invoke twice because the file entry is cached on the first run
        receiver.run();

        EasyMock.verify(adapter, interceptor);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        clean();
        monitor = EasyMock.createNiceMock(ReceiverMonitor.class);
        interceptor = EasyMock.createMock(Interceptor.class);
        adapter = EasyMock.createMock(ServiceAdapter.class);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        clean();
    }

    private void clean() throws IOException {
        FileHelper.deleteDirectory(location);
        FileHelper.deleteDirectory(errorDirectory);
        FileHelper.deleteDirectory(archiveDirectory);
    }

    private FileSystemReceiver createReceiver(String filter, Strategy strategy) {
        ReceiverConfiguration configuration =
                new ReceiverConfiguration("id", location, filter, strategy, errorDirectory, archiveDirectory, interceptor, adapter, 10, monitor);
        FileSystemReceiver receiver = new FileSystemReceiver(configuration);
        receiver.createDirectories();
        return receiver;
    }

    private void createFile(String name) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(location, name));
        } finally {
            IOHelper.closeQuietly(writer);
        }
    }


}
