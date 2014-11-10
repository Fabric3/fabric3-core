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
import java.nio.ByteBuffer;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class FileAppenderTestCase extends TestCase {
    private File file;
    private File backup;
    private ByteBuffer buffer;

    public void testRollFile() throws Exception {
        RollStrategy strategy = EasyMock.createMock(RollStrategy.class);
        EasyMock.expect(strategy.checkRoll(file)).andReturn(true);
        EasyMock.expect(strategy.getBackup(file)).andReturn(backup);
        EasyMock.replay(strategy);

        FileAppender appender = new FileAppender(file, strategy, false);
        try {
            appender.start();

            assertFalse(backup.exists());
            appender.write(buffer);
            assertTrue(backup.exists());
        } finally {
            appender.stop();
        }
    }

    public void setUp() throws Exception {
        super.setUp();
        file = new File("f3rolling.log");
        backup = new File("f3rolling.bak");
        file.createNewFile();
        buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 'x');
    }

    public void tearDown() throws Exception {
        super.tearDown();
        file.delete();
        backup.delete();
    }
}
