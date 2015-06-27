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
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.oasisopen.sca.annotation.Reference;

/**
 * Utilities used by the Domain for introspecting information from a contribution.
 */
public class ContributionHelperImpl implements ContributionHelper {
    private MetaDataStore metadataStore;
    private HostInfo hostInfo;

    public ContributionHelperImpl(@Reference MetaDataStore metadataStore, @Reference HostInfo hostInfo) {
        this.metadataStore = metadataStore;
        this.hostInfo = hostInfo;
    }

    public List<Composite> getDeployables(Set<Contribution> contributions) {
        List<Composite> deployables = new ArrayList<>();
        for (Contribution contribution : contributions) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                    if (!(entry.getValue() instanceof Composite)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"}) ResourceElement<QNameSymbol, Composite> element = (ResourceElement<QNameSymbol, Composite>) entry;
                    QName name = element.getSymbol().getKey();
                    Composite composite = element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        if (deployable.getName().equals(name)) {
                            List<RuntimeMode> deployableModes = deployable.getRuntimeModes();
                            // only add deployables that are set to boot in the current runtime mode and where the environment matches
                            if (deployableModes.contains(hostInfo.getRuntimeMode())) {
                                List<String> environments = deployable.getEnvironments();
                                if (environments.isEmpty() || environments.contains(hostInfo.getEnvironment())) {
                                    deployables.add(composite);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return deployables;
    }

    public Set<Contribution> findContributions(List<URI> uris) {
        Set<Contribution> contributions = new LinkedHashSet<>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = metadataStore.find(uri);
            if (contribution == null) {
                throw new AssertionError("Contribution not found for: " + uri);
            }
            contributions.add(contribution);
        }
        return contributions;
    }


}
