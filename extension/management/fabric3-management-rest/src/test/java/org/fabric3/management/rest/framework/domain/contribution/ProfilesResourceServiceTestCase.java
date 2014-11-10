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
 */
package org.fabric3.management.rest.framework.domain.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 *
 */
public class ProfilesResourceServiceTestCase extends TestCase {
    private static final URI PROFILE_URI = URI.create("profile");
    private ProfilesResourceService service;
    private ContributionService contributionService;
    private MetaDataStore store;

    @SuppressWarnings({"unchecked"})
    public void testGetProfiles() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.getContributions()).andReturn(Collections.singleton(contribution));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/contributions")).atLeastOnce();
        EasyMock.replay(contributionService, store, request);


        Resource resource = service.getProfiles(request);
        Set<URI> profiles = (Set<URI>) resource.getProperties().get("profiles");
        assertTrue(profiles.contains(PROFILE_URI));
        EasyMock.verify(contributionService, store, request);
    }

    @SuppressWarnings({"unchecked"})
    public void testCreateProfile() throws Exception {
        URI profileUri = URI.create("theprofile");
        URI contributionUri = URI.create("contribution1.jar");

        EasyMock.expect(contributionService.exists(EasyMock.eq(contributionUri))).andReturn(false);
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andReturn(contributionUri);
        contributionService.registerProfile(EasyMock.eq(profileUri), EasyMock.isA(List.class));
        contributionService.installProfile(profileUri);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/theprofile").atLeastOnce();
        ClassLoader loader = getClass().getClassLoader();
        InputStream resourceStream = loader.getResourceAsStream("org/fabric3/management/rest/framework/domain/contribution/test.jar");
        MockStream mockStream = new MockStream(resourceStream);
        EasyMock.expect(request.getInputStream()).andReturn(mockStream).atLeastOnce();

        EasyMock.replay(contributionService, store, request);

        Response response = service.createProfile(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("/theprofile", response.getHeaders().get("Location"));
        EasyMock.verify(contributionService, store, request);

    }

    @SuppressWarnings({"unchecked"})
    public void testDeleteProfile() throws Exception {
        URI profileUri = URI.create("theprofile");

        contributionService.uninstallProfile(profileUri);
        contributionService.removeProfile(profileUri);

        EasyMock.replay(contributionService, store);

        service.deleteProfile("theprofile");
        EasyMock.verify(contributionService, store);

    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ContributionsResourceMonitor monitor = EasyMock.createNiceMock(ContributionsResourceMonitor.class);
        EasyMock.replay(monitor);

        contributionService = EasyMock.createMock(ContributionService.class);
        store = EasyMock.createMock(MetaDataStore.class);

        service = new ProfilesResourceService(contributionService, store, monitor);
    }

    private Contribution createContribution(URI contributionUri) {
        Contribution contribution = new Contribution(contributionUri);
        QName compositeName = new QName("test", "composite");
        Deployable deployable = new Deployable(compositeName);
        contribution.getManifest().addDeployable(deployable);
        contribution.setState(ContributionState.INSTALLED);
        contribution.addProfile(PROFILE_URI);
        return contribution;
    }


    private class MockStream extends ServletInputStream {
        private InputStream stream;

        private MockStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return stream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return stream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        @Override
        public void mark(int readlimit) {
            stream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }
    }


}
