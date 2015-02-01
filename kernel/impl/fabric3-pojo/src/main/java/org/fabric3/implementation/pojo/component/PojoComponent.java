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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.objectfactory.ComponentObjectFactory;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Base class for Java component implementations.
 */
public abstract class PojoComponent implements ScopedComponent {
    private URI uri;
    private ImplementationManagerFactory factory;
    private ScopeContainer scopeContainer;
    private QName deployable;
    private boolean eager;
    private ImplementationManager implementationManager;
    private URI classLoaderId;
    private MonitorLevel level = MonitorLevel.INFO;
    private AtomicBoolean recreate = new AtomicBoolean(true);
    private Object cachedInstance;

    public PojoComponent(URI componentId, ImplementationManagerFactory factory, ScopeContainer scopeContainer, QName deployable, boolean eager) {
        this.uri = componentId;
        this.factory = factory;
        this.scopeContainer = scopeContainer;
        this.deployable = deployable;
        this.eager = eager;
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

    public QName getDeployable() {
        return deployable;
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

    public ObjectFactory<Object> createObjectFactory() {
        return new ComponentObjectFactory(this);
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
     * Sets an object factory.
     *
     * @param injectable    the InjectableAttribute identifying the component reference, property or context artifact the object factory creates instances for
     * @param objectFactory the object factory
     */
    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory) {
        setObjectFactory(injectable, objectFactory, InjectionAttributes.EMPTY_ATTRIBUTES);
    }

    /**
     * Sets an object factory.
     *
     * @param injectable    the injectable identifying the component reference, property or context artifact the object factory creates instances for
     * @param objectFactory the object factory
     * @param attributes    the injection attributes
     */
    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
        factory.setObjectFactory(injectable, objectFactory, attributes);
        List<Object> instances = scopeContainer.getActiveInstances(this);
        String name = injectable.getName();
        for (Object instance : instances) {
            getImplementationManager().updated(instance, name);
        }
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        recreate.set(true);
    }

    public void removeObjectFactory(Injectable injectable) {
        factory.removeObjectFactory(injectable);
        String name = injectable.getName();
        List<Object> instances = scopeContainer.getActiveInstances(this);
        for (Object instance : instances) {
            getImplementationManager().removed(instance, name);
        }
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        recreate.set(true);
    }

    public ObjectFactory<?> getObjectFactory(Injectable injectable) {
        return factory.getObjectFactory(injectable);
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
