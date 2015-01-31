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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.oasisopen.sca.ServiceReference;
import static org.fabric3.container.web.spi.WebApplicationActivator.OASIS_CONTEXT_ATTRIBUTE;

/**
 * A component whose implementation is a web application.
 */
public class WebComponent implements Component {

    private final URI uri;
    private URI classLoaderId;
    private ClassLoader classLoader;
    private InjectorFactory injectorFactory;
    private final WebApplicationActivator activator;
    private ChannelProxyService channelProxyService;
    // injection site name to <artifact name, injection site>
    private final Map<String, Map<String, InjectionSite>> siteMappings;
    private final WireProxyService proxyService;
    private final QName groupId;
    private final Map<String, ObjectFactory<?>> propertyFactories;
    private HostInfo info;
    private final Map<String, ObjectFactory<?>> objectFactories;
    private final URI archiveUri;
    private String contextUrl;
    private MonitorLevel level = MonitorLevel.INFO;

    public WebComponent(URI uri,
                        String contextUrl,
                        QName deployable,
                        URI archiveUri,
                        ClassLoader classLoader,
                        InjectorFactory injectorFactory,
                        WebApplicationActivator activator,
                        WireProxyService wireProxyService,
                        ChannelProxyService channelProxyService,
                        Map<String, ObjectFactory<?>> propertyFactories,
                        Map<String, Map<String, InjectionSite>> injectorMappings,
                        HostInfo info) {
        this.uri = uri;
        this.contextUrl = contextUrl;
        this.archiveUri = archiveUri;
        this.classLoader = classLoader;
        this.injectorFactory = injectorFactory;
        this.activator = activator;
        this.channelProxyService = channelProxyService;
        this.siteMappings = injectorMappings;
        this.proxyService = wireProxyService;
        this.groupId = deployable;
        this.propertyFactories = propertyFactories;
        this.info = info;
        objectFactories = new ConcurrentHashMap<>();
    }

    public URI getUri() {
        return uri;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public String getName() {
        return uri.toString();
    }

    public MonitorLevel getLevel() {
        return level;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    public void start() throws ContainerException {
        Map<String, List<Injector<?>>> injectors = new HashMap<>();
        injectorFactory.createInjectorMappings(injectors, siteMappings, objectFactories, classLoader);
        injectorFactory.createInjectorMappings(injectors, siteMappings, propertyFactories, classLoader);
        OASISWebComponentContext oasisContext = new OASISWebComponentContext(this, info);
        Map<String, ObjectFactory<?>> contextFactories = new HashMap<>();

        SingletonObjectFactory<org.oasisopen.sca.ComponentContext> oasisComponentContextFactory
                = new SingletonObjectFactory<org.oasisopen.sca.ComponentContext>(oasisContext);
        contextFactories.put(OASIS_CONTEXT_ATTRIBUTE, oasisComponentContextFactory);

        injectorFactory.createInjectorMappings(injectors, siteMappings, contextFactories, classLoader);
        // activate the web application
        activator.activate(contextUrl, archiveUri, classLoaderId, injectors, oasisContext);
    }

    public void stop() throws ContainerException {
        activator.deactivate(archiveUri);
    }

    public void startUpdate() {

    }

    public void endUpdate() {

    }

    public void attachWire(String name, Wire wire) throws ContainerException {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new ContainerException("Injection site not found for: " + name);
        }
        Class<?> type;
        try {
            type = classLoader.loadClass(sites.values().iterator().next().getType());
        } catch (ClassNotFoundException e) {
            throw new ContainerException("Reference type not found for: " + name, e);
        }
        ObjectFactory<?> factory = createWireFactory(type, wire);
        attach(name, factory);
    }

    public void attach(String name, ObjectFactory<?> factory) throws ContainerException {
        objectFactories.put(name, factory);
    }

    public void connect(String name, ChannelConnection connection) throws ContainerException {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new ContainerException("Injection site not found for: " + name);
        }
        Class<?> type;
        try {
            type = classLoader.loadClass(sites.values().iterator().next().getType());
        } catch (ClassNotFoundException e) {
            throw new ContainerException("Producer type not found for: " + name, e);
        }
        ObjectFactory<?> factory = createChannelFactory(type, connection);
        attach(name, factory);
    }

    public QName getDeployable() {
        return groupId;
    }

    public <B> B getProperty(Class<B> type, String propertyName) throws ContainerException {
        ObjectFactory<?> factory = propertyFactories.get(propertyName);
        if (factory != null) {
            return type.cast(factory.getInstance());
        } else {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public <B, R extends ServiceReference<B>> R cast(B target) {
        return (R) proxyService.cast(target);
    }

    private <B> ObjectFactory<B> createWireFactory(Class<B> interfaze, Wire wire) throws ContainerException {
        return proxyService.createObjectFactory(interfaze, wire, null);
    }

    private <B> ObjectFactory<B> createChannelFactory(Class<B> interfaze, ChannelConnection connection) throws ContainerException {
        return channelProxyService.createObjectFactory(interfaze, connection);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

}
