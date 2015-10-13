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
package org.fabric3.fabric.node;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.UrlContributionSource;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.node.Domain;
import org.fabric3.spi.container.channel.ChannelResolver;
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
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public <T> T getChannel(Class<T> interfaze, String name) {
        try {
            return channelResolver.getProducer(interfaze, name);
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(Composite composite) {
        try {
            provisioner.deploy(composite);
            return this;
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(String name, Object instance, Class<?>... interfaces) {
        try {
            provisioner.deploy(name, instance, interfaces);
            return this;
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(Component<?> component) {
        try {
            provisioner.deploy(component);
            return this;
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain undeploy(QName name) {
        try {
            provisioner.undeploy(name);
            return this;
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain undeploy(String name) {
        try {
            provisioner.undeploy(name);
            return this;
        } catch (Fabric3Exception e) {
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
        } catch (URISyntaxException | Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain undeploy(URL url) {
        try {
            URI uri = url.toURI();
            domain.undeploy(uri);
            contributionService.uninstall(uri);
            contributionService.remove(uri);
            return this;
        } catch (URISyntaxException | Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public Domain deploy(Channel channel) {
        try {
            provisioner.deploy(channel);
            return this;
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
