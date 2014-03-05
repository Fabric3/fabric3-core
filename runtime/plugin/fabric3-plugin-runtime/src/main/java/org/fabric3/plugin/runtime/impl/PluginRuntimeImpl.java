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
package org.fabric3.plugin.runtime.impl;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.contribution.ContributionException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.repository.Repository;
import org.fabric3.api.host.runtime.InitializationException;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.runtime.DefaultRuntime;
import org.fabric3.plugin.api.contribution.PluginContributionSource;
import org.fabric3.plugin.api.runtime.PluginHostInfo;
import org.fabric3.plugin.api.runtime.PluginRuntime;
import org.fabric3.plugin.api.runtime.PluginRuntimeConfiguration;
import org.fabric3.spi.container.component.ComponentException;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 *
 */
public class PluginRuntimeImpl<T extends PluginHostInfo> extends DefaultRuntime implements PluginRuntime {
    private static final URI CONTRIBUTION_URI = URI.create("iTestContribution");

    private RepositorySystem system;
    private RepositorySystemSession session;

    public PluginRuntimeImpl(PluginRuntimeConfiguration configuration) {
        super(configuration);
        system = configuration.getSystem();
        session = configuration.getSession();
    }

    @SuppressWarnings("unchecked")
    public PluginHostInfo getHostInfo() {
        return (T) super.getHostInfo();
    }

    public void deploy(URL base, QName qName) throws ContributionException, DeploymentException {
        PluginContributionSource source = new PluginContributionSource(CONTRIBUTION_URI, base);
        // contribute the Maven project to the application domain
        ContributionService contributionService = getComponent(ContributionService.class, Names.CONTRIBUTION_SERVICE_URI);
        Domain domain = getComponent(Domain.class, Names.APPLICATION_DOMAIN_URI);
        URI uri = contributionService.store(source);
        contributionService.install(uri);

        MetaDataStore metaDataStore = getMetaDataStore();
        Contribution contribution = metaDataStore.find(uri);
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        if (deployables.isEmpty()) {
            // No deployables specified, activate the test composite in the domain. If a test composite does not exist, an exception will be raised
            domain.include(qName);
            startContext(qName);
        } else {
            // include deployables
            domain.include(Collections.singletonList(uri));

            Deployable qNameDeployable = new Deployable(qName);
            if (!deployables.contains(qNameDeployable)) {
                // deploy the test composite if one exists and it is not defined as a deployable
                QNameSymbol symbol = new QNameSymbol(qName);
                ResourceElement<QNameSymbol, Composite> resourceElement = metaDataStore.find(uri, Composite.class, symbol);
                if (resourceElement != null) {
                    domain.include(resourceElement.getValue(), false);
                    startContext(qName);
                }
            }
        }
    }

    public void startContext(QName deployable) throws DeploymentException {
        WorkContextCache.getAndResetThreadWorkContext();
        try {
            getScopeContainer().startContext(deployable);
        } catch (ComponentException e) {
            throw new DeploymentException(e);
        }
    }

    protected Repository createRepository() throws InitializationException {
        return new AetherRepository(system, session);
    }
}
