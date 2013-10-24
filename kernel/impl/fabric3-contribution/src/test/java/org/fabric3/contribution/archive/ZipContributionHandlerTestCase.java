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
package org.fabric3.contribution.archive;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.archive.ArtifactResourceCallback;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;

/**
 *
 */
public class ZipContributionHandlerTestCase extends TestCase {
    private ZipContributionHandler handler;
    private Loader loader;

    public void testCanProcess() throws Exception {
        Contribution contribution = createContribution();
        assertTrue(handler.canProcess(contribution));
    }

    public void testProcessManifest() throws Exception {
        Contribution contribution = createContribution();

        ContributionManifest manifest = new ContributionManifest();
        loader.load(EasyMock.isA(Source.class), EasyMock.eq(ContributionManifest.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andReturn(manifest);
        EasyMock.replay(loader);

        handler.processManifest(contribution, new DefaultIntrospectionContext());

        EasyMock.verify(loader);
    }

    public void testIterateAllContents() throws Exception {
        Contribution contribution = createContribution();

        ArtifactResourceCallback callback = EasyMock.createMock(ArtifactResourceCallback.class);
        callback.onResource(EasyMock.isA(Resource.class));
        EasyMock.expectLastCall().times(6);  // 6 items in the jar minus the contribution manifest
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback);

        EasyMock.verify(callback);
    }

    public void testExcludeContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/test1.*.composite")));

        ArtifactResourceCallback callback = EasyMock.createMock(ArtifactResourceCallback.class);
        callback.onResource(EasyMock.isA(Resource.class));
        // Should only be 5 items in the jar minus the contribution manifest and the excluded file
        EasyMock.expectLastCall().times(5);
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback);

        EasyMock.verify(callback);
    }

    public void testDirectoryContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/.*")));

        ArtifactResourceCallback callback = EasyMock.createMock(ArtifactResourceCallback.class);
        // no contents should be scanned
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback);

        EasyMock.verify(callback);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ContentTypeResolver resolver = EasyMock.createMock(ContentTypeResolver.class);
        EasyMock.expect(resolver.getContentType(EasyMock.isA(String.class))).andReturn("application/xml").anyTimes();
        EasyMock.replay(resolver);
        loader = EasyMock.createMock(Loader.class);

        handler = new ZipContributionHandler(loader, resolver);
    }

    private Contribution createContribution() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/2/testWithLibraries.jar");
        URI uri = URI.create("test");
        UrlSource source = new UrlSource(location);
        Contribution contribution = new Contribution(uri, source, location, -1, null, false);
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
        return contribution;
    }

}
