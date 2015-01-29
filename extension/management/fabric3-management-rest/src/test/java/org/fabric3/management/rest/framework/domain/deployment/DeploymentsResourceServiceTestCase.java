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
package org.fabric3.management.rest.framework.domain.deployment;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.failure.AssemblyFailure;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 *
 */
public class DeploymentsResourceServiceTestCase extends TestCase {
    private static final QName COMPOSITE_NAME = new QName("test", "composite");

    private DeploymentsResourceService service;
    private Domain domain;
    private MetaDataStore store;

    @SuppressWarnings({"unchecked"})
    public void testGetDeployments() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.getContributions()).andReturn(Collections.singleton(contribution));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/deployments")).atLeastOnce();
        EasyMock.replay(store, request);

        Resource resource = service.getDeployments(request);
        List<URI> list = (List<URI>) resource.getProperties().get("contributions");
        assertTrue(list.contains(contributionUri));
        EasyMock.verify(store, request);
    }

    @SuppressWarnings({"unchecked"})
    public void testDeploy() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);

        domain.include(COMPOSITE_NAME);
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();
        EasyMock.replay(store, request);

        Response resource = service.deploy(request);
        assertEquals(HttpStatus.CREATED, resource.getStatus());
        assertEquals("/thecontribution", resource.getHeaders().get("Location"));
        EasyMock.verify(store, request);
    }

    @SuppressWarnings({"unchecked", "ThrowableInstanceNeverThrown"})
    public void testDeployErrors() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();

        List<AssemblyFailure> errors = new ArrayList<>();
        errors.add(new MockFailure());
        domain.include(COMPOSITE_NAME);
        EasyMock.expectLastCall().andThrow(new AssemblyException(errors));

        EasyMock.replay(store, domain, request);

        Response resource = service.deploy(request);
        assertEquals(HttpStatus.VALIDATION_ERROR, resource.getStatus());

        assertNotNull(resource.getEntity());
        EasyMock.verify(store, domain, request);
    }

    @SuppressWarnings({"unchecked"})
    public void testUnDeploy() throws Exception {
        URI contributionUri = URI.create("thecontribution");

        domain.undeploy(contributionUri, false);
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.replay(store, request);

        service.undeploy("thecontribution");

        EasyMock.verify(store, request);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        DomainResourceMonitor monitor = EasyMock.createNiceMock(DomainResourceMonitor.class);
        EasyMock.replay(monitor);

        domain = EasyMock.createMock(Domain.class);
        store = EasyMock.createMock(MetaDataStore.class);

        service = new DeploymentsResourceService(domain, store, monitor);
    }

    private Contribution createContribution(URI contributionUri) {
        Contribution contribution = new Contribution(contributionUri);
        Deployable deployable = new Deployable(COMPOSITE_NAME);
        contribution.getManifest().addDeployable(deployable);
        contribution.setState(ContributionState.INSTALLED);
        contribution.acquireLock(COMPOSITE_NAME);
        return contribution;
    }

    private class MockFailure extends AssemblyFailure {

        public MockFailure() {
            super(URI.create("components"), URI.create("contribution1"), Collections.emptyList());
        }

        public String getMessage() {
            return "";
        }

    }
}
