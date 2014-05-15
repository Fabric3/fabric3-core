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
package org.fabric3.implementation.web.runtime;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentException;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
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

    public void start() throws ComponentException {
        try {
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
        } catch (InjectionCreationException | WebApplicationActivationException e) {
            throw new WebComponentStartException("Error starting web component: " + uri.toString(), e);
        }

    }

    public void stop() throws ComponentException {
        try {
            activator.deactivate(archiveUri);
        } catch (WebApplicationActivationException e) {
            throw new WebComponentStopException("Error stopping web component: " + uri.toString(), e);
        }
    }

    public void startUpdate() {

    }

    public void endUpdate() {

    }

    public void attachWire(String name, Wire wire) throws ObjectCreationException {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new ObjectCreationException("Injection site not found for: " + name);
        }
        Class<?> type;
        try {
            type = classLoader.loadClass(sites.values().iterator().next().getType());
        } catch (ClassNotFoundException e) {
            throw new ObjectCreationException("Reference type not found for: " + name, e);
        }
        ObjectFactory<?> factory = createWireFactory(type, wire);
        attach(name, factory);
    }

    public void attach(String name, ObjectFactory<?> factory) throws ObjectCreationException {
        objectFactories.put(name, factory);
    }

    public void connect(String name, ChannelConnection connection) throws ObjectCreationException {
        Map<String, InjectionSite> sites = siteMappings.get(name);
        if (sites == null || sites.isEmpty()) {
            throw new ObjectCreationException("Injection site not found for: " + name);
        }
        Class<?> type;
        try {
            type = classLoader.loadClass(sites.values().iterator().next().getType());
        } catch (ClassNotFoundException e) {
            throw new ObjectCreationException("Producer type not found for: " + name, e);
        }
        ObjectFactory<?> factory = createChannelFactory(type, connection);
        attach(name, factory);
    }

    public QName getDeployable() {
        return groupId;
    }

    public <B> B getProperty(Class<B> type, String propertyName) throws ObjectCreationException {
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

    private <B> ObjectFactory<B> createWireFactory(Class<B> interfaze, Wire wire) throws ObjectCreationException {
        try {
            return proxyService.createObjectFactory(interfaze, wire, null);
        } catch (ProxyCreationException e) {
            throw new ObjectCreationException(e);
        }
    }

    private <B> ObjectFactory<B> createChannelFactory(Class<B> interfaze, ChannelConnection connection) throws ObjectCreationException {
        try {
            return channelProxyService.createObjectFactory(interfaze, connection);
        } catch (ProxyCreationException e) {
            throw new ObjectCreationException(e);
        }
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

}
