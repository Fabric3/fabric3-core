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
package org.fabric3.monitor.impl.writer;

import java.nio.ByteBuffer;
import java.util.TimeZone;

import junit.framework.TestCase;
import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 *
 */
public class MonitorEntryWriterTestCase extends TestCase {
    private long timestamp;
    private ByteBuffer buffer;
    private TimestampWriter timestampWriter;

    public void testWriteString() throws Exception {
        MonitorEntryWriter.write(MonitorLevel.SEVERE, timestamp, "This is a {0}", buffer, timestampWriter, new Object[]{"test"});
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
        assertTrue(string.contains("This is a test"));
    }

    public void testWriteNumeric() throws Exception {
        MonitorEntryWriter.write(MonitorLevel.SEVERE, timestamp, "This is a {0}", buffer, timestampWriter, new Object[]{1});
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
        assertTrue(string.contains("This is a 1"));
    }

    public void testWriteBoolean() throws Exception {
        MonitorEntryWriter.write(MonitorLevel.SEVERE, timestamp, "This is a {0}", buffer, timestampWriter, new Object[]{true});
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
        assertTrue(string.contains("This is a true"));
    }

    public void testEmptyTemplate() throws Exception {
        MonitorEntryWriter.write(MonitorLevel.SEVERE, timestamp, null, buffer, timestampWriter, null);
        String string = new String(buffer.array());
        assertTrue(string.contains("SEVERE"));
    }

    public void setUp() throws Exception {
        super.setUp();
        timestamp = System.currentTimeMillis();
        buffer = ByteBuffer.allocate(200);
        timestampWriter = new TimestampWriter("%d:%m:%Y %H:%i:%s.%F", TimeZone.getDefault());
    }
}
