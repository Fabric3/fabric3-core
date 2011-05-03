/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Utilities used by the Domain for introspecting information from a contribution.
 *
 * @version $Rev$ $Date$
 */
public class ContributionHelperImpl implements ContributionHelper {
    private static final String PLAN_NAMESPACE = "urn:fabric3.org:extension:plan";
    private MetaDataStore metadataStore;
    private HostInfo hostInfo;

    public ContributionHelperImpl(@Reference MetaDataStore metadataStore, @Reference HostInfo hostInfo) {
        this.metadataStore = metadataStore;
        this.hostInfo = hostInfo;
    }

    public List<Composite> getDeployables(Set<Contribution> contributions) {
        List<Composite> deployables = new ArrayList<Composite>();
        for (Contribution contribution : contributions) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                    if (!(entry.getValue() instanceof Composite)) {
                        continue;
                    }
                    @SuppressWarnings({"unchecked"})
                    ResourceElement<QNameSymbol, Composite> element = (ResourceElement<QNameSymbol, Composite>) entry;
                    QName name = element.getSymbol().getKey();
                    Composite composite = element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        if (deployable.getName().equals(name)) {
                            List<RuntimeMode> deployableModes = deployable.getRuntimeModes();
                            // only add deployables that are set to boot in the current runtime mode
                            if (deployableModes.contains(hostInfo.getRuntimeMode())) {
                                deployables.add(composite);
                            }
                            break;
                        }
                    }
                }
            }
        }
        return deployables;
    }

    public Composite findComposite(QName deployable) throws DeploymentException {
        QNameSymbol symbol = new QNameSymbol(deployable);
        ResourceElement<QNameSymbol, Composite> element = metadataStore.find(Composite.class, symbol);
        if (element == null) {
            String id = deployable.toString();
            throw new DeployableNotFoundException("Deployable not found: " + id, id);
        }

        return element.getValue();
    }

    public DeploymentPlan findDefaultPlan(QName deployable) {
        // default to first found deployment plan in a contribution if one is not specifed for a distributed deployment
        QNameSymbol symbol = new QNameSymbol(deployable);
        Contribution contribution = metadataStore.find(Composite.class, symbol).getResource().getContribution();
        return findDefaultPlan(contribution);
    }

    public DeploymentPlan findDefaultPlan(Contribution contribution) {
        DeploymentPlan plan;
        List<DeploymentPlan> plans = new ArrayList<DeploymentPlan>();
        getDeploymentPlans(contribution, plans);
        if (!plans.isEmpty()) {
            plan = plans.get(0);
        } else {
            return null;
        }
        return plan;
    }

    public DeploymentPlan findPlan(String plan) throws DeploymentException {
        QName planName = new QName(PLAN_NAMESPACE, plan);
        QNameSymbol symbol = new QNameSymbol(planName);
        ResourceElement<QNameSymbol, DeploymentPlan> element = metadataStore.find(DeploymentPlan.class, symbol);
        if (element == null) {
            return null;
        }
        return element.getValue();
    }

    public Set<Contribution> findContributions(List<URI> uris) {
        Set<Contribution> contributions = new LinkedHashSet<Contribution>(uris.size());
        for (URI uri : uris) {
            Contribution contribution = metadataStore.find(uri);
            if (contribution == null) {
                throw new AssertionError("Contribution not found for: " + uri);
            }
            contributions.add(contribution);
        }
        return contributions;
    }

    public void lock(Set<Contribution> contributions) throws CompositeAlreadyDeployedException {
        for (Contribution contribution : contributions) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName name = deployable.getName();
                // check if the deployable has already been deployed by querying the lock owners
                if (contribution.getLockOwners().contains(name)) {
                    throw new CompositeAlreadyDeployedException("Composite has already been deployed: " + name);
                }
                contribution.acquireLock(name);
            }
        }
    }

    public void releaseLocks(Set<Contribution> contributions) {
        for (Contribution contribution : contributions) {
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName name = deployable.getName();
                if (contribution.getLockOwners().contains(name)) {
                    contribution.releaseLock(name);
                }
            }
        }
    }

    private void getDeploymentPlans(Contribution contribution, List<DeploymentPlan> plans) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> entry : resource.getResourceElements()) {
                if (!(entry.getValue() instanceof DeploymentPlan)) {
                    continue;
                }
                @SuppressWarnings({"unchecked"})
                ResourceElement<QNameSymbol, DeploymentPlan> element = (ResourceElement<QNameSymbol, DeploymentPlan>) entry;
                DeploymentPlan plan = element.getValue();
                plans.add(plan);
            }
        }
    }

}
