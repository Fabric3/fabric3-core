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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Handles the /domain/deployments resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /deployments - Returns deployed contributions</ul>
 *  <li>PUT /deployments/contribution/{uri} - Deploys a contribution</ul>
 *  <li>DELETE /deployments/contribution/{uri} - Un-deploys a contribution</ul>
 * </ul>
 * </pre>
 *  Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/deployments")
public class DeploymentsResourceService {
    private MetaDataStore store;

    public DeploymentsResourceService(@Reference MetaDataStore store) {
        this.store = store;
    }

    @ManagementOperation(path = "/")
    public Resource getDeployments(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        Set<Contribution> contributions = store.getContributions();
        List<URI> list = new ArrayList<>();
        for (Contribution contribution : contributions) {
            if (contribution.getLockOwners().isEmpty() || contribution.getManifest().isExtension()) {
                // not deployed or not deployed to the application domain
                continue;
            }
            URI uri = contribution.getUri();
            list.add(uri);
        }
        resource.setProperty("contributions", list);
        return resource;
    }

}
