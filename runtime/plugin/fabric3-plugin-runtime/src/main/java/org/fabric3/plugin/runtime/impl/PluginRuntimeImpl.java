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
