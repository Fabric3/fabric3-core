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
import static org.fabric3.spi.contribution.Constants.EXPLODED_CONTENT_TYPE;

/**
 *
 */
public class ExplodedArchiveContributionHandlerTestCase extends TestCase {
    private ExplodedArchiveContributionHandler handler;
    private Loader loader;
    private IntrospectionContext context;

    public void testCanProcess() throws Exception {
        Contribution contribution = createContribution();
        assertTrue(handler.canProcess(contribution));
    }

    public void testProcessManifest() throws Exception {
        ContributionManifest manifest = new ContributionManifest();
        loader.load(EasyMock.isA(Source.class), EasyMock.eq(ContributionManifest.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andReturn(manifest);
        EasyMock.replay(loader);
        Contribution contribution = createContribution();

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        handler.processManifest(contribution, context);

        EasyMock.verify(loader);
    }

    public void testIterateAllContents() throws Exception {
        Contribution contribution = createContribution();

        ArtifactResourceCallback callback = EasyMock.createMock(ArtifactResourceCallback.class);
        callback.onResource(EasyMock.isA(Resource.class));
        EasyMock.expectLastCall().times(2);  // 2 items in the archive minus the contribution manifest
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback, context);

        EasyMock.verify(callback);
    }

    public void testExcludeContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/test1.*.composite")));

        ArtifactResourceCallback callback = EasyMock.createMock(ArtifactResourceCallback.class);
        callback.onResource(EasyMock.isA(Resource.class));
        // Should only be 1 item in the archive minus the contribution manifest and the excluded file
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback, context);

        EasyMock.verify(callback);
    }

    public void testDirectoryContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/.*")));

        ArtifactResourceCallback callback = EasyMock.createMock(ArtifactResourceCallback.class);
        // no contents should be scanned
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback, context);

        EasyMock.verify(callback);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ContentTypeResolver resolver = EasyMock.createMock(ContentTypeResolver.class);
        EasyMock.expect(resolver.getContentType(EasyMock.isA(String.class))).andReturn("application/xml").anyTimes();
        loader = EasyMock.createMock(Loader.class);
        EasyMock.replay(resolver);
        ClassLoader classLoader = getClass().getClassLoader();
        context = new DefaultIntrospectionContext(URI.create("test"), classLoader);

        handler = new ExplodedArchiveContributionHandler(loader, resolver);
    }

    private Contribution createContribution() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./exploded");
        URI uri = URI.create("test");
        UrlSource source = new UrlSource(location);
        Contribution contribution = new Contribution(uri, source, location, -1, EXPLODED_CONTENT_TYPE, false);
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
        return contribution;
    }

}
