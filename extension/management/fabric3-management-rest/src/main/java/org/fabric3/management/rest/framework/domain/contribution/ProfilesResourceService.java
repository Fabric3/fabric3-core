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
import java.net.URI;
import java.util.HashSet;
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
 * Handles the /domain/contributions/profiles resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /contributions/profiles - Returns installed profiles</ul>
 *  <li>PUT /contributions/profiles/{uri} - Installs a profile</ul>
 *  <li>DELETE /contributions/profiles/{uri} - Removes the installed profile</ul>
 * </ul>
 * </pre>
 * <p/> Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/contributions/profiles")
public class ProfilesResourceService {
    private MetaDataStore store;

    public ProfilesResourceService(@Reference MetaDataStore store) {
        this.store = store;
    }

    @ManagementOperation(path = "/")
    public Resource getProfiles(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        Set<Contribution> contributions = store.getContributions();
        Set<URI> profiles = new HashSet<>();
        for (Contribution contribution : contributions) {
            profiles.addAll(contribution.getProfiles());
        }
        resource.setProperty("profiles", profiles);
        return resource;
    }

}
