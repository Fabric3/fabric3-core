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
package org.fabric3.node.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.node.Domain;
import org.fabric3.api.host.contribution.ContributionNotFoundException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.contribution.RemoveException;
import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.contribution.UninstallException;
import org.fabric3.api.host.contribution.UrlContributionSource;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default Node Domain implementation.
 */
public class NodeDomain implements Domain {
    private Provisioner provisioner;
    private ServiceResolver serviceResolver;
    private ChannelResolver channelResolver;
    private ContributionService contributionService;
    private org.fabric3.api.host.domain.Domain domain;

    public NodeDomain(@Reference Provisioner provisioner,
                      @Reference ServiceResolver serviceResolver,
                      @Reference ChannelResolver channelResolver,
                      @Reference ContributionService contributionService,
                      @Reference(name = "domain") org.fabric3.api.host.domain.Domain domain) {
        this.provisioner = provisioner;
        this.serviceResolver = serviceResolver;
        this.channelResolver = channelResolver;
        this.contributionService = contributionService;
        this.domain = domain;
    }

    public <T> T getService(Class<T> interfaze) {
        try {
            return serviceResolver.resolve(interfaze);
        } catch (ResolverException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public <T> T getChannel(Class<T> interfaze, String name) {
        try {
            return channelResolver.resolve(interfaze, name);
        } catch (ResolverException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(Composite composite) {
        try {
            provisioner.deploy(composite);
            return this;
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }


    public Domain deploy(String name, Object instance, Class<?>... interfaces) {
        try {
            provisioner.deploy(name, instance, interfaces);
            return this;
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(ComponentDefinition<?> definition) {
        try {
            provisioner.deploy(definition);
            return this;
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain undeploy(QName name) {
        try {
            provisioner.undeploy(name);
            return this;
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain undeploy(String name) {
        try {
            provisioner.undeploy(name);
            return this;
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(URL url) {
        try {
            URI uri = url.toURI();
            UrlContributionSource source = new UrlContributionSource(uri, url, false);
            contributionService.store(source);
            contributionService.install(uri);
            domain.include(Collections.singletonList(uri));
            return this;
        } catch (URISyntaxException | org.fabric3.api.host.domain.DeploymentException | InstallException | ContributionNotFoundException | StoreException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain undeploy(URL url) {
        try {
            URI uri = url.toURI();
            domain.undeploy(uri, true);
            contributionService.uninstall(uri);
            contributionService.remove(uri);
            return this;
        } catch (URISyntaxException | RemoveException | UninstallException | org.fabric3.api.host.domain.DeploymentException | ContributionNotFoundException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(ChannelDefinition definition) {
        try {
            provisioner.deploy(definition);
            return this;
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
