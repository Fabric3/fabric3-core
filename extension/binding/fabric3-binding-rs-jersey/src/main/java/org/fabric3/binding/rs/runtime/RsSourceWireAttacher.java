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
package org.fabric3.binding.rs.runtime;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.spi.container.JavaMethodInvokerFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.rs.provision.AuthenticationType;
import org.fabric3.binding.rs.provision.RsSourceDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 *
 */
@EagerInit
public class RsSourceWireAttacher implements SourceWireAttacher<RsSourceDefinition> {
    private ServletHost servletHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private RsContainerManager containerManager;
    private RsWireAttacherMonitor monitor;
    private Level logLevel = Level.WARNING;

    public RsSourceWireAttacher(@Reference ServletHost servletHost,
                                @Reference ClassLoaderRegistry registry,
                                @Reference RsContainerManager containerManager,
                                @Reference BasicAuthenticator authenticator,
                                @Monitor RsWireAttacherMonitor monitor) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = registry;
        this.containerManager = containerManager;
        this.monitor = monitor;
        // TODO make realm configurable
        overrideDefaultInvoker(authenticator);
        setDebugLevel();
    }

    @Property(required = false)
    public void setLogLevel(String level) {
        this.logLevel = Level.parse(level);
    }

    public void attach(RsSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WireAttachException {
        URI sourceUri = source.getUri();
        RsContainer container = containerManager.get(sourceUri);
        if (container == null) {
            container = new RsContainer(getClass().getClassLoader());
            containerManager.register(sourceUri, container);
            String mapping = creatingMappingUri(sourceUri);
            if (servletHost.isMappingRegistered(mapping)) {
                // wire reprovisioned
                servletHost.unregisterMapping(mapping);
            }
            servletHost.registerMapping(mapping, container);
        }

        try {
            provision(source, wire, container);
            monitor.provisionedEndpoint(sourceUri);
        } catch (ClassNotFoundException e) {
            String name = source.getRsClass();
            throw new WireAttachException("Unable to load interface class " + name, sourceUri, null, e);
        }
    }

    public void detach(RsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        URI sourceUri = source.getUri();
        String mapping = creatingMappingUri(sourceUri);
        servletHost.unregisterMapping(mapping);
        containerManager.unregister(sourceUri);
        monitor.removedEndpoint(sourceUri);
    }

    public void attachObjectFactory(RsSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        throw new AssertionError();
    }

    public void detachObjectFactory(RsSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    private String creatingMappingUri(URI sourceUri) {
        String servletMapping = sourceUri.getPath();
        if (!servletMapping.endsWith("/*")) {
            servletMapping = servletMapping + "/*";
        }
        return servletMapping;
    }

    private void provision(RsSourceDefinition sourceDefinition, Wire wire, RsContainer container) throws ClassNotFoundException {
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(sourceDefinition.getClassLoaderId());
        Map<String, InvocationChain> invocationChains = new HashMap<String, InvocationChain>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            invocationChains.put(operation.getName(), chain);
        }

        Class<?> interfaze = classLoader.loadClass(sourceDefinition.getRsClass());
        ResourceInstance instance = new ResourceInstance(invocationChains, authenticate(sourceDefinition));
        container.addResource(interfaze, instance);
    }

    private boolean authenticate(RsSourceDefinition sourceDefinition) {
        if (AuthenticationType.BASIC == sourceDefinition.getAuthenticationType()) {
            return true;
        } else if (AuthenticationType.STATEFUL_FORM == sourceDefinition.getAuthenticationType()) {
            throw new UnsupportedOperationException();
        } else if (AuthenticationType.DIGEST == sourceDefinition.getAuthenticationType()) {
            throw new UnsupportedOperationException();
        }
        return false;
    }

    /**
     * Overrides the default Jersey invoker which reflectively calls methods on a Java instance with one that passes an invocation down a component's
     * invocation chain.
     *
     * @param authenticator the security authenticator
     */
    private void overrideDefaultInvoker(BasicAuthenticator authenticator) {
        try {
            Field field = JavaMethodInvokerFactory.class.getDeclaredField("defaultInstance");
            field.setAccessible(true);
            field.set(null, new F3MethodInvoker(authenticator));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void setDebugLevel() {
        Logger logger = Logger.getLogger("com.sun.jersey");
        logger.setLevel(logLevel);
    }

}
