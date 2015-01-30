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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.contribution.scanner.impl.DirectoryResource;
import org.fabric3.contribution.scanner.spi.FileSystemResource;

/**
 *
 */
public class DirectoryResourceTestCase extends TestCase {

    public void testChanges() throws Exception {
        DirectoryResource resource = new DirectoryResource(new File("test"));
        FileSystemResource fileSystemResource = EasyMock.createMock(FileSystemResource.class);
        long time = System.currentTimeMillis() + 200000;
        EasyMock.expect(fileSystemResource.getTimestamp()).andReturn(time);
        EasyMock.expect(fileSystemResource.getTimestamp()).andReturn(time);
        EasyMock.expect(fileSystemResource.getTimestamp()).andReturn(time + 1000);
        EasyMock.replay(fileSystemResource);
        assertFalse(resource.isChanged());
        resource.addResource(fileSystemResource);
        assertTrue(resource.isChanged());
        assertFalse(resource.isChanged());
        assertTrue(resource.isChanged());
        EasyMock.verify(fileSystemResource);
    }
}
