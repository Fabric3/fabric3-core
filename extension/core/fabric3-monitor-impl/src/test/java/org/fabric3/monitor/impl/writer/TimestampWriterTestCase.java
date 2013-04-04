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
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 *
 */
public class TimestampWriterTestCase extends TestCase {

    public void testWriteTimestamp() throws Exception {
        TimestampWriter writer = new TimestampWriter("%Y:%m:%d %H:%i:%s.%F", TimeZone.getDefault());
        ByteBuffer buffer = ByteBuffer.allocateDirect(100);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.JANUARY, 12, 11, 11, 11);

        int written = writer.write(calendar.getTimeInMillis(), buffer);

        buffer.flip();
        byte[] serialized = new byte[buffer.limit()];
        buffer.get(serialized);

        assertTrue(new String(serialized).contains("2013:01:12 11:11:11."));
        assertEquals(serialized.length, written);
    }
}
