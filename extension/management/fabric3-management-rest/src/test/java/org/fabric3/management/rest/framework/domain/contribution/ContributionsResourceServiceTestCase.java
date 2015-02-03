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

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 *
 */
public class ContributionsResourceServiceTestCase extends TestCase {
    private ContributionsResourceService service;
    private MetaDataStore store;

    public void testGetContribution() throws Exception {
        URI contributionUri = URI.create("contribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);

        EasyMock.replay(store);

        ContributionResource resource = service.getContribution("contribution");
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        assertTrue(resource.getDeployables().contains(deployables.get(0).getName()));
        assertEquals(ContributionState.INSTALLED.toString(), resource.getState());
        EasyMock.verify(store);
    }

    public void testGetContributionNotFound() throws Exception {
        URI contributionUri = URI.create("contribution");
        EasyMock.expect(store.find(contributionUri)).andReturn(null);

        EasyMock.replay(store);

        try {
            service.getContribution("contribution");
            fail();
        } catch (ResourceException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
        EasyMock.verify(store);
    }

    @SuppressWarnings("unchecked")
    public void testGetContributions() throws Exception {
        URI contributionUri = URI.create("thecontribution");
        Contribution contribution = createContribution(contributionUri);

        EasyMock.expect(store.getContributions()).andReturn(Collections.singleton(contribution));

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/contributions")).atLeastOnce();
        EasyMock.replay(store, request);

        Resource resource = service.getContributions(request);
        List<ContributionStatus> contributions = (List<ContributionStatus>) resource.getProperties().get("contributions");
        ContributionStatus status = contributions.get(0);
        assertEquals("thecontribution", status.getLink().getName());
        assertEquals("edit", status.getLink().getRel());
        assertEquals("http:/localhost/management/domain/contributions/contribution/thecontribution", status.getLink().getHref().toString());
        EasyMock.verify(store, request);
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

        store = EasyMock.createMock(MetaDataStore.class);

        service = new ContributionsResourceService(store);
    }


}
