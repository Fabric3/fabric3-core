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
package org.fabric3.binding.ws.metro.runtime.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.util.FileHelper;

/**
 *
 */
public class ArtifactCacheImplTestCase extends TestCase {
    private ArtifactCacheImpl cache;

    public void testCache() throws Exception {
        URI uri = URI.create("test");
        InputStream stream = new ByteArrayInputStream("this is a test".getBytes());
        cache.cache(uri, stream);
        URL url = cache.get(uri);
        assertNotNull(url);
        InputStream ret = url.openStream();
        ret.close();
        assertNotNull(cache.get(uri));
        cache.remove(uri);
        assertNull(cache.get(uri));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getTempDir()).andReturn(new File("tmp_cache"));
        EasyMock.replay(info);
        cache = new ArtifactCacheImpl(info);
        cache.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("tmp_cache"));
    }
}
