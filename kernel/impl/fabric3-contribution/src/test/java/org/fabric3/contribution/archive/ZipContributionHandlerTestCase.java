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
import java.util.function.Consumer;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;

/**
 *
 */
public class ZipContributionHandlerTestCase extends TestCase {
    private ZipContributionHandler handler;
    private Loader loader;
    private IntrospectionContext context;

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

    @SuppressWarnings("unchecked")
    public void testIterateAllContents() throws Exception {
        Contribution contribution = createContribution();

        Consumer callback = EasyMock.createMock(Consumer.class);
        callback.accept(EasyMock.isA(Resource.class));
        EasyMock.expectLastCall().times(6);  // 6 items in the jar minus the contribution manifest
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback, context);

        EasyMock.verify(callback);
    }

    @SuppressWarnings("unchecked")
    public void testExcludeContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/test1.*.composite")));

        Consumer callback = EasyMock.createMock(Consumer.class);
        callback.accept(EasyMock.isA(Resource.class));
        // Should only be 5 items in the jar minus the contribution manifest and the excluded file
        EasyMock.expectLastCall().times(5);
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback, context);

        EasyMock.verify(callback);
    }

    public void testDirectoryContents() throws Exception {
        Contribution contribution = createContribution();
        contribution.getManifest().setScanExcludes(Collections.singletonList(Pattern.compile("META-INF/.*")));

        Consumer callback = EasyMock.createMock(Consumer.class);
        // no contents should be scanned
        EasyMock.replay(callback);

        handler.iterateArtifacts(contribution, callback, context);

        EasyMock.verify(callback);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ContentTypeResolver resolver = EasyMock.createMock(ContentTypeResolver.class);
        EasyMock.expect(resolver.getContentType(EasyMock.isA(String.class))).andReturn("application/xml").anyTimes();
        EasyMock.replay(resolver);
        loader = EasyMock.createMock(Loader.class);

        ClassLoader classLoader = getClass().getClassLoader();
        context = new DefaultIntrospectionContext(URI.create("test"), classLoader);

        handler = new ZipContributionHandler(loader, resolver);
    }

    private Contribution createContribution() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/2/testWithLibraries.jar");
        URI uri = URI.create("test");
        UrlSource source = new UrlSource(location);
        Contribution contribution = new Contribution(uri, source, location, -1, null);
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
        return contribution;
    }

}
