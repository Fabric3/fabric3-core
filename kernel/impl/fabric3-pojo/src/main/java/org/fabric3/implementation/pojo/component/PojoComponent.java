/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.component;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.implementation.pojo.injection.ComponentObjectFactory;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactory;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.type.java.Injectable;

/**
 * Base class for Component implementations based on Java objects.
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class
 */
public abstract class PojoComponent<T> implements AtomicComponent<T> {
    private final URI uri;
    private final InstanceFactoryProvider<T> provider;
    private final ScopeContainer scopeContainer;
    private final QName deployable;
    private final boolean eager;
    private final long maxIdleTime;
    private final long maxAge;
    private InstanceFactory<T> instanceFactory;
    private URI classLoaderId;
    private MonitorLevel level = MonitorLevel.INFO;

    public PojoComponent(URI componentId,
                         InstanceFactoryProvider<T> provider,
                         ScopeContainer scopeContainer,
                         QName deployable,
                         boolean eager,
                         long maxIdleTime,
                         long maxAge) {
        this.uri = componentId;
        this.provider = provider;
        this.scopeContainer = scopeContainer;
        this.deployable = deployable;
        this.eager = eager;
        this.maxIdleTime = maxIdleTime;
        this.maxAge = maxAge;
    }

    public void start() throws ComponentException {
        scopeContainer.register(this);
    }

    public void stop() throws ComponentException {
        instanceFactory = null;
        scopeContainer.unregister(this);
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

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        return getInstanceFactory().newInstance(workContext);
    }

    @SuppressWarnings({"unchecked"})
    public ObjectFactory<T> createObjectFactory() {
        return new ComponentObjectFactory(this, scopeContainer);
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public Class<T> getImplementationClass() {
        return provider.getImplementationClass();
    }

    /**
     * Sets an object factory.
     *
     * @param injectable    the InjectableAttribute identifying the component reference, property or context artifact the object factory creates
     *                      instances for
     * @param objectFactory the object factory
     */
    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory) {
        setObjectFactory(injectable, objectFactory, null);
    }

    /**
     * Sets an object factory.
     *
     * @param injectable    the injectable identifying the component reference, property or context artifact the object factory creates instances for
     * @param objectFactory the object factory
     * @param key           key value for a Map reference
     */
    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, Object key) {
        scopeContainer.updated(this, injectable.getName());
        provider.setObjectFactory(injectable, objectFactory, key);
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first 
        // instance has been created.
        instanceFactory = null;
    }

    public void removeObjectFactory(Injectable injectable) {
        scopeContainer.removed(this, injectable.getName());
        provider.removeObjectFactory(injectable);
        // Clear the instance factory as it has changed and will need to be re-created. This can happen if reinjection occurs after the first
        // instance has been created.
        instanceFactory = null;
    }

    public ObjectFactory<?> getObjectFactory(Injectable injectable) {
        return provider.getObjectFactory(injectable);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

    private InstanceFactory<T> getInstanceFactory() {
        if (instanceFactory == null) {
            instanceFactory = provider.createFactory();
        }
        return instanceFactory;
    }

}
