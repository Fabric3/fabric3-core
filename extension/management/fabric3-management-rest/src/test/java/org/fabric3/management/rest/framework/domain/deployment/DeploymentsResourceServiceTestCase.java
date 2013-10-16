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
package org.fabric3.management.rest.framework.domain.deployment;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.host.failure.AssemblyFailure;
import org.fabric3.api.host.domain.Domain;
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

        domain.activateDefinitions(contributionUri);
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

        domain.activateDefinitions(contributionUri);

        List<AssemblyFailure> errors = new ArrayList<AssemblyFailure>();
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
