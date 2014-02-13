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
package org.fabric3.fabric.deployment.generator.collator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.deployment.generator.GenerationType;
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
