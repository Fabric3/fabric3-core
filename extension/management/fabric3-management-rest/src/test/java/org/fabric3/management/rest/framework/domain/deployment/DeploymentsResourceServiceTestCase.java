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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 *
 */
public class DeploymentsResourceServiceTestCase extends TestCase {
    private static final QName COMPOSITE_NAME = new QName("test", "composite");

    private DeploymentsResourceService service;
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        store = EasyMock.createMock(MetaDataStore.class);

        service = new DeploymentsResourceService(store);
    }

    private Contribution createContribution(URI contributionUri) {
        Contribution contribution = new Contribution(contributionUri);
        Deployable deployable = new Deployable(COMPOSITE_NAME);
        contribution.getManifest().addDeployable(deployable);
        contribution.setState(ContributionState.INSTALLED);
        contribution.acquireLock(COMPOSITE_NAME);
        return contribution;
    }

}
