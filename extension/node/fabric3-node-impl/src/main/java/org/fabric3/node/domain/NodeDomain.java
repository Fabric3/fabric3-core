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
package org.fabric3.node.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.fabric3.api.model.type.component.ChannelDefinition;
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
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        } catch (StoreException e) {
            throw new ServiceRuntimeException(e);
        } catch (ContributionNotFoundException e) {
            throw new ServiceRuntimeException(e);
        } catch (InstallException e) {
            throw new ServiceRuntimeException(e);
        } catch (org.fabric3.api.host.domain.DeploymentException e) {
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
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        } catch (ContributionNotFoundException e) {
            throw new ServiceRuntimeException(e);
        } catch (org.fabric3.api.host.domain.DeploymentException e) {
            throw new ServiceRuntimeException(e);
        } catch (UninstallException e) {
            throw new ServiceRuntimeException(e);
        } catch (RemoveException e) {
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
