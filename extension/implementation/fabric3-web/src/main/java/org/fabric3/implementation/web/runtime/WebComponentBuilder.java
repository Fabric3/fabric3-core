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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.web.runtime;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.implementation.web.provision.WebComponentDefinition;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates a web component on a runtime node.
 */
@EagerInit
public class WebComponentBuilder implements ComponentBuilder<WebComponentDefinition, WebComponent> {
    private WireProxyService wireProxyService;
    private ChannelProxyService channelProxyService;
    private WebApplicationActivator activator;
    private InjectorFactory injectorFactory;
    private HostInfo info;

    public WebComponentBuilder(@Reference WireProxyService wireProxyService,
                               @Reference ChannelProxyService channelProxyService,
                               @Reference WebApplicationActivator activator,
                               @Reference InjectorFactory injectorFactory,
                               @Reference HostInfo info) {
        this.wireProxyService = wireProxyService;
        this.channelProxyService = channelProxyService;
        this.activator = activator;
        this.injectorFactory = injectorFactory;
        this.info = info;
    }

    public WebComponent build(WebComponentDefinition definition) throws ContainerException {
        URI uri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        // TODO fix properties
        Map<String, ObjectFactory<?>> propertyFactories = Collections.emptyMap();
        URI classLoaderId = definition.getClassLoaderId();
        Map<String, Map<String, InjectionSite>> injectorMappings = definition.getInjectionSiteMappings();
        ClassLoader cl = activator.getWebComponentClassLoader(classLoaderId);
        String contextUrl = definition.getContextUrl();
        return new WebComponent(uri,
                                contextUrl,
                                deployable,
                                classLoaderId,
                                cl,
                                injectorFactory,
                                activator,
                                wireProxyService,
                                channelProxyService,
                                propertyFactories,
                                injectorMappings,
                                info);
    }

    public void dispose(WebComponentDefinition definition, WebComponent component) throws ContainerException {
        // no-op
    }
}
