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
package org.fabric3.binding.file.runtime.receiver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.file.InvalidDataException;
import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.host.util.IOHelper;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageImpl;
import org.fabric3.spi.container.wire.Interceptor;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
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
