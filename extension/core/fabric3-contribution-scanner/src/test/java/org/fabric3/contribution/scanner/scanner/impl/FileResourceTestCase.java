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
package org.fabric3.contribution.scanner.scanner.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.fabric3.contribution.scanner.spi.FileResource;

/**
 *
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
