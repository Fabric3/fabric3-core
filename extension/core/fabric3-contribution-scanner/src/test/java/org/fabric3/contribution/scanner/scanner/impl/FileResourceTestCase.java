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
package org.fabric3.contribution.scanner.scanner.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.fabric3.contribution.scanner.spi.FileResource;

/**
 * @version $Rev$ $Date$
 */
public class FileResourceTestCase extends TestCase {
    private File file;

    public void testChanged() throws Exception {
        FileResource resource = new FileResource(file);
        resource.checkpoint();
        assertFalse(resource.isChanged());
        Thread.sleep(1000);
        writeFile("testtest");
        assertTrue(resource.isChanged());
    }


    protected void setUp() throws Exception {
        super.setUp();
        writeFile("test");
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        if (file.exists()) {
            file.delete();
        }
    }

    private void writeFile(String contents) throws IOException {
        FileOutputStream stream = null;
        try {
            file = new File("fileresourcetest.txt");
            stream = new FileOutputStream(file);
            stream.write(contents.getBytes());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
