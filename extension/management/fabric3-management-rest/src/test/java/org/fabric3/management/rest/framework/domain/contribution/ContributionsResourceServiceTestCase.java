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
package org.fabric3.management.rest.framework.domain.contribution;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.DuplicateContributionException;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.contribution.UnresolvedImportException;
import org.fabric3.host.failure.ValidationFailure;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.introspection.validation.InvalidContributionException;

/**
 *
 */
public class ContributionsResourceServiceTestCase extends TestCase {
    private ContributionsResourceService service;
    private ContributionService contributionService;
    private MetaDataStore store;

    public void testGetContribution() throws Exception {
        URI contributionUri = URI.create("contribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);

        EasyMock.replay(contributionService, store);

        ContributionResource resource = service.getContribution("contribution");
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        assertTrue(resource.getDeployables().contains(deployables.get(0).getName()));
        assertEquals(ContributionState.INSTALLED.toString(), resource.getState());
        EasyMock.verify(contributionService, store);
    }

    public void testGetContributionNotFound() throws Exception {
        URI contributionUri = URI.create("contribution");
        EasyMock.expect(store.find(contributionUri)).andReturn(null);

        EasyMock.replay(contributionService, store);

        try {
            service.getContribution("contribution");
            fail();
        } catch (ResourceException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
        EasyMock.verify(contributionService, store);
    }

    public void testGetContributions() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.getContributions()).andReturn(Collections.singleton(contribution));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/contributions")).atLeastOnce();
        EasyMock.replay(contributionService, store, request);

        Resource resource = service.getContributions(request);
        List<ContributionStatus> contributions = (List<ContributionStatus>) resource.getProperties().get("contributions");
        ContributionStatus status = contributions.get(0);
        assertEquals("thecontribution", status.getLink().getName());
        assertEquals("edit", status.getLink().getRel());
        assertEquals("http:/localhost/management/domain/contributions/contribution/thecontribution", status.getLink().getHref().toString());
        EasyMock.verify(contributionService, store, request);
    }


    public void testCreateContribution() throws Exception {
        URI contributionUri = URI.create("thecontribution");

        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andReturn(contributionUri);
        contributionService.install(contributionUri);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();
        EasyMock.expect(request.getInputStream()).andReturn(new MockStream()).atLeastOnce();

        EasyMock.replay(contributionService, store, request);


        Response response = service.createContribution(request);
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("/thecontribution", response.getHeaders().get("Location"));
        EasyMock.verify(contributionService, store, request);
    }

    public void testCreateDuplicateContribution() throws Exception {
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andThrow(new DuplicateContributionException("duplicate"));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();
        EasyMock.expect(request.getInputStream()).andReturn(new MockStream()).atLeastOnce();

        EasyMock.replay(contributionService, store, request);

        try {
            service.createContribution(request);
            fail();
        } catch (ResourceException e) {
            // expected
            assertEquals(HttpStatus.CONFLICT, e.getStatus());
        }
        EasyMock.verify(contributionService, store, request);
    }

    public void testCreateContributionStoreError() throws Exception {
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andThrow(new StoreException("exception"));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();
        EasyMock.expect(request.getInputStream()).andReturn(new MockStream()).atLeastOnce();

        EasyMock.replay(contributionService, store, request);

        try {
            service.createContribution(request);
            fail();
        } catch (ResourceException e) {
            // expected
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
        }
        EasyMock.verify(contributionService, store, request);
    }


    public void testCreateContributionInstallError() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andReturn(contributionUri);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();
        EasyMock.expect(request.getInputStream()).andReturn(new MockStream()).atLeastOnce();

        contributionService.install(contributionUri);
        EasyMock.expectLastCall().andThrow(new UnresolvedImportException("import"));
        EasyMock.replay(contributionService, store, request);

        try {
            service.createContribution(request);
            fail();
        } catch (ResourceException e) {
            // expected
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        }
        EasyMock.verify(contributionService, store, request);
    }

    public void testCreateContributionPropagetErrors() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andReturn(contributionUri);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/thecontribution").atLeastOnce();
        EasyMock.expect(request.getInputStream()).andReturn(new MockStream()).atLeastOnce();

        contributionService.install(contributionUri);
        List<ValidationFailure> errors = Collections.<ValidationFailure>singletonList(new MockValidationFailure());
        InvalidContributionException exception = new InvalidContributionException(errors);
        EasyMock.expectLastCall().andThrow(exception);
        EasyMock.replay(contributionService, store, request);

        try {
            service.createContribution(request);
            fail();
        } catch (ResourceException e) {
            // expected
             Map<String, List<String>> resultErrors = (Map<String, List<String>>) e.getEntity();
             assertTrue(resultErrors.get("General").contains("error"));
        }
        EasyMock.verify(contributionService, store, request);
    }

    public void testDeleteContribution() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        contributionService.uninstall(contributionUri);
        contributionService.remove(contributionUri);
        EasyMock.replay(contributionService, store);

        service.deleteContribution("thecontribution");

        EasyMock.verify(contributionService, store);
    }

    public void testDeleteContributionRemoveException() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        contributionService.uninstall(contributionUri);
        contributionService.remove(contributionUri);
        EasyMock.expectLastCall().andThrow(new RemoveException("remove"));

        EasyMock.replay(contributionService, store);
        try {
            service.deleteContribution("thecontribution");
        } catch (ResourceException e) {
            // expected
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
        }

        EasyMock.verify(contributionService, store);
    }

    private Contribution createContribution(URI contributionUri) {
        Contribution contribution = new Contribution(contributionUri);
        QName compositeName = new QName("test", "composite");
        Deployable deployable = new Deployable(compositeName);
        contribution.getManifest().addDeployable(deployable);
        contribution.setState(ContributionState.INSTALLED);
        return contribution;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ContributionsResourceMonitor monitor = EasyMock.createNiceMock(ContributionsResourceMonitor.class);
        EasyMock.replay(monitor);

        contributionService = EasyMock.createMock(ContributionService.class);
        store = EasyMock.createMock(MetaDataStore.class);

        service = new ContributionsResourceService(contributionService, store, monitor);
    }

    private class MockStream extends ServletInputStream {

        @Override
        public int read() throws IOException {
            return 0;
        }
    }

    private class MockValidationFailure extends ValidationFailure{

        @Override
        public String getMessage() {
            return "error";
        }
    }
}
