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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.management.rest.model.Link.EDIT_LINK;

/**
 * Handles the /domain/contributions resource and its sub-resources:
 * <pre>
 *  - GET /contributions - Returns installed contributions
 * </pre>
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/contributions")
public class ContributionsResourceService {
    private MetaDataStore store;

    public ContributionsResourceService(@Reference MetaDataStore store) {
        this.store = store;
    }

    @ManagementOperation(path = "/")
    public Resource getContributions(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        Set<Contribution> contributions = store.getContributions();
        List<ContributionStatus> list = new ArrayList<>();
        for (Contribution contribution : contributions) {
            URI uri = contribution.getUri();
            Link link = createContributionLink(uri, request);
            String state = contribution.getState().toString();
            ContributionStatus status = new ContributionStatus(uri, state, link);
            list.add(status);
        }
        resource.setProperty("contributions", list);
        return resource;
    }

    @ManagementOperation(path = "contribution")
    public ContributionResource getContribution(String uri) throws ResourceException {
        URI contributionUri = URI.create(uri);
        Contribution contribution = store.find(contributionUri);
        if (contribution == null) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Contribution not found: " + uri);
        }
        String state = contribution.getState().toString();
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        List<QName> names = new ArrayList<>();
        for (Deployable deployable : deployables) {
            QName name = deployable.getName();
            names.add(name);
        }
        return new ContributionResource(contributionUri, state, names);
    }

    private Link createContributionLink(URI contributionUri, HttpServletRequest request) {
        String uri = contributionUri.toString();
        String requestUrl = ResourceHelper.getRequestUrl(request);
        URL url = ResourceHelper.createUrl(requestUrl + "/contribution/" + uri);
        return new Link(uri, EDIT_LINK, url);
    }

}
