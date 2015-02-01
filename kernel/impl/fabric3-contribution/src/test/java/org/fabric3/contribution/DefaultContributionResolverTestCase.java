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
package org.fabric3.contribution;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.repository.ArtifactCache;

/**
 *
 */
public class DefaultContributionResolverTestCase extends TestCase {
    private MetaDataStore store;
    private ArtifactCache cache;
    private ContributionResolverExtension extension;
    private DefaultContributionResolver resolver;
    private URI uri;
    private URL location;

    public void testResolveAgainstStore() throws Exception {
        Contribution contribution = new Contribution(uri, null, location, -1, null, false);
        EasyMock.expect(store.find(uri)).andReturn(contribution);
        EasyMock.replay(store, cache, extension);

        assertEquals(location, resolver.resolve(uri));

        EasyMock.verify(store, cache, extension);
    }

    public void testResolveAgainstCache() throws Exception {
        EasyMock.expect(store.find(uri)).andReturn(null);
        EasyMock.expect(cache.get(uri)).andReturn(location);
        EasyMock.replay(store, cache, extension);

        assertEquals(location, resolver.resolve(uri));

        EasyMock.verify(store, cache, extension);
    }

    public void testResolveAgainstExtension() throws Exception {
        EasyMock.expect(store.find(uri)).andReturn(null);
        EasyMock.expect(cache.get(uri)).andReturn(null);
        ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
        EasyMock.expect(extension.resolve(uri)).andReturn(stream);
        EasyMock.expect(cache.cache(uri, stream)).andReturn(location);
        EasyMock.replay(store, cache, extension);

        resolver.setExtensions(Collections.singletonList(extension));
        assertEquals(location, resolver.resolve(uri));

        EasyMock.verify(store, cache, extension);
    }

    public void testRelease() throws Exception {
        EasyMock.expect(cache.remove(uri)).andReturn(true);
        EasyMock.replay(store, cache, extension);
        resolver.release(uri);
        EasyMock.verify(store, cache, extension);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uri = URI.create("contribution");
        location = new URL("file://test");

        store = EasyMock.createMock(MetaDataStore.class);
        cache = EasyMock.createMock(ArtifactCache.class);
        extension = EasyMock.createMock(ContributionResolverExtension.class);

        resolver = new DefaultContributionResolver(store, cache);
    }
}
