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
package org.fabric3.fabric.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.api.model.type.RuntimeMode;

public class RepositoryImplTestCase extends TestCase {
    private RepositoryImpl repository;
    private File runtimeDir;
    private File sharedDir;
    private File userDir;

    public void testStoreAndFind() throws Exception {
        URI uri = URI.create("test-resource");
        InputStream archiveStream = new ByteArrayInputStream("test".getBytes());
        repository.store(uri, archiveStream, false);
        URL contributionURL = repository.find(uri);
        assertNotNull(contributionURL);
    }

    public void testInit() throws Exception {
        createArchives();
        repository.init();
        assertTrue(repository.exists(URI.create("testRuntime.jar")));
        assertTrue(repository.exists(URI.create("testShared.jar")));
        assertTrue(repository.exists(URI.create("testUser.jar")));
    }

    public void testList() throws Exception {
        URI archiveUri = URI.create("test-resource");
        InputStream archiveStream = new ByteArrayInputStream("test".getBytes());
        repository.store(archiveUri, archiveStream, false);
        boolean found = false;
        for (URI uri : repository.list()) {
            if (uri.equals(archiveUri)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    public void testRemove() throws Exception {
        URI archiveUri = URI.create("test-resource");
        InputStream archiveStream = new ByteArrayInputStream("test".getBytes());
        repository.store(archiveUri, archiveStream, false);
        assertTrue(new File(userDir, "test-resource").exists());
        repository.remove(archiveUri);
        assertFalse(new File(userDir, "test-resource").exists());
    }

    protected void setUp() throws Exception {
        super.setUp();
        File repositoryDir = new File("repository");
        runtimeDir = new File(repositoryDir, "runtime");
        sharedDir = new File(repositoryDir, "shared");
        userDir = new File(repositoryDir, "user");

        FileHelper.forceMkdir(new File(repositoryDir, "cache"));
        FileHelper.forceMkdir(runtimeDir);
        FileHelper.forceMkdir(sharedDir);
        FileHelper.forceMkdir(userDir);
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeRepositoryDirectory()).andReturn(runtimeDir).atLeastOnce();
        EasyMock.expect(info.getExtensionsRepositoryDirectory()).andReturn(sharedDir).atLeastOnce();
        EasyMock.expect(info.getUserRepositoryDirectory()).andReturn(userDir).atLeastOnce();
        EasyMock.expect(info.getTempDir()).andReturn(null).atLeastOnce();
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();
        EasyMock.replay(info);
        repository = new RepositoryImpl(info);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.forceDelete(new File("repository"));
    }

    private void createArchives() throws IOException {
        InputStream stream = new ByteArrayInputStream("test".getBytes());
        FileHelper.write(stream, new File(runtimeDir, "testRuntime.jar"));
        FileHelper.write(stream, new File(sharedDir, "testShared.jar"));
        FileHelper.write(stream, new File(userDir, "testUser.jar"));
    }


}