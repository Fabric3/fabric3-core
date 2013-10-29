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
package org.fabric3.fabric.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;

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