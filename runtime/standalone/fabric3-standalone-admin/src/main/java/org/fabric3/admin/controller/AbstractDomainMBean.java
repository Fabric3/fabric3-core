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
package org.fabric3.admin.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.domain.DomainException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.ContributionNotFoundException;
import org.fabric3.management.domain.ContributionNotInstalledManagementException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;
import org.fabric3.management.domain.InvalidPathException;
import org.fabric3.management.domain.NoDeployablesManagementException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev$ $Date$
 */
@Management
public abstract class AbstractDomainMBean {
    protected Domain domain;
    protected MetaDataStore store;
    protected LogicalComponentManager lcm;
    protected HostInfo info;
    protected DomainMBeanMonitor monitor;
    protected String domainUri;

    public AbstractDomainMBean(Domain domain, MetaDataStore store, LogicalComponentManager lcm, HostInfo info, DomainMBeanMonitor monitor) {
        this.domain = domain;
        this.store = store;
        this.lcm = lcm;
        this.info = info;
        this.domainUri = info.getDomain().toString();
        this.monitor = monitor;
    }

    @ManagementOperation(description = "Deploys a contribution to the domain.  All contained deployables will be included in the domain composite.")
    public void deploy(URI uri) throws DeploymentManagementException {
        deploy(uri, null);
    }

    @ManagementOperation(description = "Deploys a contribution to the domain using the specified deployment plan.  All contained deployables will be "
            + "included in the domain composite.")
    public void deploy(URI uri, String plan) throws DeploymentManagementException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        try {
            domain.activateDefinitions(uri);
        } catch (DeploymentException e) {
            throw new ContributionNotInstalledManagementException(e.getMessage());
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            QName name = deployable.getName();
            try {
                if (plan == null) {
                    domain.include(name);
                } else {
                    domain.include(name, plan);
                }
            } catch (ContributionNotInstalledException e) {
                throw new ContributionNotInstalledManagementException(e.getMessage());
            } catch (AssemblyException e) {
                List<String> errors = new ArrayList<String>();
                for (AssemblyFailure error : e.getErrors()) {
                    errors.add(error.getMessage() + " (" + error.getContributionUri() + ")");
                }
                throw new InvalidDeploymentException("Error deploying " + uri, errors);
            } catch (CompositeAlreadyDeployedException e) {
                throw new ContributionNotInstalledManagementException(e.getMessage());
            } catch (DeployableNotFoundException e) {
                throw new ContributionNotInstalledManagementException(e.getMessage());
            } catch (DeploymentException e) {
                reportError(uri, e);
            }

        }
    }

    @ManagementOperation(description = "Undeploys deployables contained in a contribution")
    public void undeploy(URI uri, boolean force) throws DeploymentManagementException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        if (deployables.isEmpty()) {
            throw new NoDeployablesManagementException("The contribution does not contain any deployable composites: " + uri);
        }
        for (Deployable deployable : deployables) {
            try {
                QName name = deployable.getName();
                domain.undeploy(name, force);
            } catch (DeploymentException e) {
                reportError(uri, e);
            }

        }
        try {
            domain.deactivateDefinitions(uri);
        } catch (DeploymentException e) {
            throw new ContributionNotInstalledManagementException(e.getMessage());
        }
    }

    @ManagementOperation(description = "Returns a list of ComponentInfo instances representing the components deployed to the given composite path. "
            + "The path / is interpreted as the domain composite.")
    public List<ComponentInfo> getDeployedComponents(String path) throws InvalidPathException {
        String tokens[] = path.split("/");
        LogicalCompositeComponent currentComponent = lcm.getRootComponent();
        List<ComponentInfo> infos = new ArrayList<ComponentInfo>();
        String currentPath = domainUri;
        if (tokens.length > 0 && !domainUri.endsWith(tokens[0]) && !tokens[0].equals("/")) {
            throw new InvalidPathException("Path not found: " + path);
        }
        for (int i = 1; i < tokens.length; i++) {
            currentPath = currentPath + "/" + tokens[i];
            LogicalComponent<?> component = currentComponent.getComponent(URI.create(currentPath));
            if (component == null) {
                throw new InvalidPathException("Deployed composite not exist: " + path);
            } else if (!(component instanceof LogicalCompositeComponent)) {
                throw new InvalidPathException("Component is not a composite: " + path);
            } else {
                currentComponent = (LogicalCompositeComponent) component;
            }
        }
        for (LogicalComponent<?> component : currentComponent.getComponents()) {
            URI uri = component.getUri();
            URI contributionUri = component.getDefinition().getContributionUri();
            QName deployable = component.getDeployable();
            String zone = component.getZone();
            ComponentInfo info = new ComponentInfo(uri, contributionUri, deployable, zone);
            infos.add(info);
        }
        return infos;
    }

    protected void reportError(URI uri, DomainException e) throws DeploymentManagementException {
        monitor.error("Error deploying " + uri, e);
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        throw new DeploymentManagementException(cause.getMessage());
    }

}