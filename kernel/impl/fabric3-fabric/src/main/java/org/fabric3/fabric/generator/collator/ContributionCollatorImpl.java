/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.fabric.generator.collator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.GenerationType;
import org.fabric3.host.Names;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * @version $Rev$ $Date$
 */
public class ContributionCollatorImpl implements ContributionCollator {
    private MetaDataStore store;

    public ContributionCollatorImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    public Map<String, List<Contribution>> collateContributions(List<LogicalComponent<?>> components, GenerationType type) {
        // collate all contributions that must be provisioned as part of the change set
        Map<String, List<Contribution>> contributionsPerZone = new HashMap<String, List<Contribution>>();
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
                contributions = new ArrayList<Contribution>();
                contributionsPerZone.put(zone, contributions);
            }
            Contribution contribution = store.find(contributionUri);
            // imported contributions must also be provisioned
            List<ContributionWire<?, ?>> contributionWires = contribution.getWires();
            for (ContributionWire<?, ?> wire : contributionWires) {
                URI importedUri = wire.getExportContributionUri();
                Contribution imported = store.find(importedUri);
                if (!contributions.contains(imported)
                        && !Names.HOST_CONTRIBUTION.equals(importedUri)
                        && !Names.BOOT_CONTRIBUTION.equals(importedUri)) {
                    contributions.add(imported);
                }
            }
            if (!contributions.contains(contribution)) {
                contributions.add(contribution);
            }
        }
        return contributionsPerZone;
    }

}
