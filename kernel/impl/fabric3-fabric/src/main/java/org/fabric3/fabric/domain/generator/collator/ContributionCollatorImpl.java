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
package org.fabric3.fabric.domain.generator.collator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.domain.generator.GenerationType;
import org.fabric3.api.host.Names;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class ContributionCollatorImpl implements ContributionCollator {
    private MetaDataStore store;

    public ContributionCollatorImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    public Map<String, List<Contribution>> collateContributions(List<LogicalComponent<?>> components, GenerationType type) {
        // collate all contributions that must be provisioned as part of the change set
        Map<String, List<Contribution>> contributionsPerZone = new HashMap<>();
        for (LogicalComponent<?> component : components) {
            if (type != GenerationType.FULL) {
                if (GenerationType.INCREMENTAL == type && LogicalState.NEW != component.getState()) {
                    continue;
                } else if (GenerationType.UNDEPLOY == type && LogicalState.MARKED != component.getState()) {
                    continue;
                }
            }
            URI contributionUri = component.getDefinition().getContributionUri();
            String zone = component.getZone();
            List<Contribution> contributions = contributionsPerZone.get(zone);
            if (contributions == null) {
                contributions = new ArrayList<>();
                contributionsPerZone.put(zone, contributions);
            }
            Contribution contribution = store.find(contributionUri);
            if (include(contribution, contributions)) {
                collateDependencies(contribution, contributions);
            }
        }
        return contributionsPerZone;
    }

    /**
     * Collates transitive dependencies for a contribution, including the contribution.
     *
     * @param contribution  the contribution
     * @param contributions the list of transitive dependencies, including the contribution
     */
    private void collateDependencies(Contribution contribution, List<Contribution> contributions) {
        // imported contributions must also be provisioned
        List<ContributionWire<?, ?>> contributionWires = contribution.getWires();
        for (ContributionWire<?, ?> wire : contributionWires) {
            URI importedUri = wire.getExportContributionUri();
            Contribution imported = store.find(importedUri);
            if (include(imported, contributions)) {
                collateDependencies(imported, contributions);
            }
        }
        if (!contributions.contains(contribution)) {
            contributions.add(contribution);
        }
    }

    /**
     * Returns true if the contribution should be added to the list of contributions, i.e. it is not already present and is not the host or boot
     * contribution.
     *
     * @param contribution  the contribution
     * @param contributions the list of contributions
     * @return true if the contribution should be included
     */
    private boolean include(Contribution contribution, List<Contribution> contributions) {
        URI uri = contribution.getUri();
        return !contributions.contains(contribution) && !Names.HOST_CONTRIBUTION.equals(uri) && !Names.BOOT_CONTRIBUTION.equals(uri);
    }

}
