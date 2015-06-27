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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.component;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.container.injection.InjectionAttributes;

/**
 * Base class for Java component implementations.
 */
public abstract class PojoComponent implements ScopedComponent {
    private URI uri;
    private ImplementationManagerFactory factory;
    private ScopeContainer scopeContainer;
    private boolean eager;
    private ImplementationManager implementationManager;
    private URI contributionUri;
    private MonitorLevel level = MonitorLevel.INFO;
    private AtomicBoolean recreate = new AtomicBoolean(true);
    private Object cachedInstance;

    public PojoComponent(URI componentId, ImplementationManagerFactory factory, ScopeContainer scopeContainer, boolean eager, URI contributionUri) {
        this.uri = componentId;
        this.factory = factory;
        this.scopeContainer = scopeContainer;
        this.eager = eager;
        this.contributionUri = contributionUri;
    }

    public void start() throws Fabric3Exception {
        scopeContainer.register(this);
    }

    public void stop() throws Fabric3Exception {
        implementationManager = null;
        cachedInstance = null;
        scopeContainer.unregister(this);
    }

    public void startUpdate() {
        factory.startUpdate();
    }

    public void endUpdate() {
        factory.endUpdate();
    }

    public URI getUri() {
        return uri;
    }

    public URI getContributionUri() {
        return contributionUri;
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

    public boolean isEagerInit() {
        return eager;
    }

    public Object getInstance() throws Fabric3Exception {
        if (cachedInstance != null) {
            return cachedInstance;
        }
        return scopeContainer.getInstance(this);
    }

    public void releaseInstance(Object instance) throws Fabric3Exception {
        scopeContainer.releaseInstance(this, instance);
    }

    public Object createInstance() throws Fabric3Exception {
        if (recreate.getAndSet(false)) {
            implementationManager = null;
        }
        Object instance = getImplementationManager().newInstance();
        if (Scope.COMPOSITE == scopeContainer.getScope()) {
            cachedInstance = instance;
        }
        return instance;
    }

    public Supplier<Object> createSupplier() {
        return this::getInstance;
    }

    public void startInstance(Object instance) throws Fabric3Exception {
        getImplementationManager().start(instance);
    }

    public void stopInstance(Object instance) throws Fabric3Exception {
        cachedInstance = null;
        getImplementationManager().stop(instance);
    }

    public void reinject(Object instance) throws Fabric3Exception {
        getImplementationManager().reinject(instance);
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public Class<?> getImplementationClass() {
        return factory.getImplementationClass();
    }

    /**
     * Sets a Supplier
     *
     * @param injectable the InjectableAttribute identifying the component reference, property or context artifact the Supplier creates instances for
     * @param supplier   the Supplier
     */
    public void setSupplier(Injectable injectable, Supplier<?> supplier) {
        setSupplier(injectable, supplier, InjectionAttributes.EMPTY_ATTRIBUTES);
    }

    /**
     * Sets a Supplier.
     *
     * @param injectable the injectable identifying the component reference, property or context artifact the Supplier creates instances for
     * @param supplier   the Supplier
     * @param attributes the injection attributes
     */
    public void setSupplier(Injectable injectable, Supplier<?> supplier, InjectionAttributes attributes) {
        factory.setSupplier(injectable, supplier, attributes);
        List<Object> instances = scopeContainer.getActiveInstances(this);
        String name = injectable.getName();
        for (Object instance : instances) {
            getImplementationManager().updated(instance, name);
        }
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        recreate.set(true);
    }

    public void removeSupplier(Injectable injectable) {
        factory.removeSupplier(injectable);
        String name = injectable.getName();
        List<Object> instances = scopeContainer.getActiveInstances(this);
        for (Object instance : instances) {
            getImplementationManager().removed(instance, name);
        }
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        recreate.set(true);
    }

    public Supplier<?> getSupplier(Injectable injectable) {
        return factory.getObjectSupplier(injectable);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

    private ImplementationManager getImplementationManager() {
        if (implementationManager == null) {
            implementationManager = factory.createManager();
        }
        return implementationManager;
    }

}
