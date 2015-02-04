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
import java.util.function.Supplier;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.injection.Injector;
import org.fabric3.spi.container.wire.Wire;
import static org.fabric3.container.web.spi.WebApplicationActivator.OASIS_CONTEXT_ATTRIBUTE;

/**
 * A component whose implementation is a web application.
 */
public class WebComponent implements Component {

    private final URI uri;
    private URI contributionUri;
    private ClassLoader classLoader;
    private InjectorFactory injectorFactory;
    private final WebApplicationActivator activator;
    private ChannelProxyService channelProxyService;
    // injection site name to <artifact name, injection site>
    private final Map<String, Map<String, InjectionSite>> siteMappings;
    private final WireProxyService proxyService;
    private final QName groupId;
    private final Map<String, Supplier<?>> propertySuppliers;
    private HostInfo info;
    private final Map<String, Supplier<?>> suppliers;
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
                        Map<String, Supplier<?>> propertySuppliers,
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
        this.propertySuppliers = propertySuppliers;
        this.info = info;
        suppliers = new ConcurrentHashMap<>();
    }

    public URI getUri() {
        return uri;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public void setContributionUri(URI uri) {
        this.contributionUri = uri;
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

    public void start() throws Fabric3Exception {
        Map<String, List<Injector<?>>> injectors = new HashMap<>();
        injectorFactory.createInjectorMappings(injectors, siteMappings, suppliers, classLoader);
        injectorFactory.createInjectorMappings(injectors, siteMappings, propertySuppliers, classLoader);
        OASISWebComponentContext oasisContext = new OASISWebComponentContext(this, info);
        Map<String, Supplier<?>> contextSuppliers = new HashMap<>();

        contextSuppliers.put(OASIS_CONTEXT_ATTRIBUTE, () -> oasisContext);

        injectorFactory.createInjectorMappings(injectors, siteMappings, contextSuppliers, classLoader);
        // activate the web application
        activator.activate(contextUrl, archiveUri, classLoader, injectors);
    }

    public void stop() throws Fabric3Exception {
        activator.deactivate(archiveUri);
    }

    public void startUpdate() {

    }

    public void endUpdate() {

    }

    public void attachWire(String name, Wire wire) throws Fabric3Exception {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new Fabric3Exception("Injection site not found for: " + name);
        }
        Class<?> type = sites.values().iterator().next().getType();
        Supplier<?> supplier = createWireFactory(type, wire);
        attach(name, supplier);
    }

    public void attach(String name, Supplier<?> supplier) throws Fabric3Exception {
        suppliers.put(name, supplier);
    }

    public void connect(String name, ChannelConnection connection) throws Fabric3Exception {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new Fabric3Exception("Injection site not found for: " + name);
        }
        Class<?> type = sites.values().iterator().next().getType();
        Supplier<?> factory = createChannelFactory(type, connection);
        attach(name, factory);
    }

    public QName getDeployable() {
        return groupId;
    }

    public <B> B getProperty(Class<B> type, String propertyName) throws Fabric3Exception {
        Supplier<?> supplier = propertySuppliers.get(propertyName);
        if (supplier != null) {
            return type.cast(supplier.get());
        } else {
            return null;
        }
    }

    private <B> Supplier<B> createWireFactory(Class<B> interfaze, Wire wire) throws Fabric3Exception {
        return proxyService.createSupplier(interfaze, wire, null);
    }

    private <B> Supplier<B> createChannelFactory(Class<B> interfaze, ChannelConnection connection) throws Fabric3Exception {
        return channelProxyService.createSupplier(interfaze, connection);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

}
