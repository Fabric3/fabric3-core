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
package org.fabric3.monitor.appender.file;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.monitor.spi.appender.Appender;

/**
 *
 */
public class FileAppenderBuilderTestCase extends TestCase {

    public void testRollingAppenderBuild() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDataDir()).andReturn(new File("test"));
        EasyMock.replay(info);

        FileAppenderBuilder builder = new FileAppenderBuilder(info);
        Appender appender = builder.build(new PhysicalFileAppenderDefinition("test", "size", 10, -1));
        assertNotNull(appender);

        EasyMock.verify(info);
    }

    public void testNoRollAppenderBuild() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDataDir()).andReturn(new File("test"));
        EasyMock.replay(info);

        FileAppenderBuilder builder = new FileAppenderBuilder(info);
        Appender appender = builder.build(new PhysicalFileAppenderDefinition("test", "none", -1, -1));
        assertNotNull(appender);

        EasyMock.verify(info);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("test"));
    }
}
