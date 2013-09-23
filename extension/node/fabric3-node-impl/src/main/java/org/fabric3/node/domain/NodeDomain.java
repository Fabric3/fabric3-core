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

import org.fabric3.api.node.Domain;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.contribution.UrlContributionSource;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default Node Domain implementation.
 */
public class NodeDomain implements Domain {
    private InstanceDeployer deployer;
    private ServiceResolver serviceResolver;
    private ContributionService contributionService;
    private org.fabric3.host.domain.Domain domain;

    public NodeDomain(@Reference InstanceDeployer deployer,
                      @Reference ServiceResolver serviceResolver,
                      @Reference ContributionService contributionService,
                      @Reference org.fabric3.host.domain.Domain domain) {
        this.deployer = deployer;
        this.serviceResolver = serviceResolver;
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
        throw new UnsupportedOperationException();
    }

    public void subscribe(Class<?> interfaze, String name, Object consumer) {
    }

    public <T> void deploy(Class<T> interfaze, T instance) {
        try {
            deployer.deploy(interfaze, instance);
        } catch (DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void deploy(URL url) {
        try {
            URI uri = url.toURI();
            UrlContributionSource source = new UrlContributionSource(uri, url, false);
            contributionService.store(source);
            contributionService.install(uri);
            domain.include(Collections.singletonList(uri));
        } catch (URISyntaxException e) {
            throw new ServiceRuntimeException(e);
        } catch (StoreException e) {
            throw new ServiceRuntimeException(e);
        } catch (ContributionNotFoundException e) {
            throw new ServiceRuntimeException(e);
        } catch (InstallException e) {
            throw new ServiceRuntimeException(e);
        } catch (org.fabric3.host.domain.DeploymentException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
