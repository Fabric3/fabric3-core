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
package org.fabric3.contribution;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.MetaDataStore;

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
