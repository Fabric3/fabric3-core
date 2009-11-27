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
package org.fabric3.implementation.pojo.reflection;

import java.util.HashSet;
import java.util.Set;

import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.Injector;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceWrapper<T> implements InstanceWrapper<T> {
    private final T instance;
    private boolean reinjectable;
    private final ClassLoader cl;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private boolean started;
    private final Injectable[] attributes;
    private final Injector<T>[] injectors;
    private final Set<Injector<T>> updatedInjectors;

    public ReflectiveInstanceWrapper(T instance,
                                     boolean reinjectable,
                                     ClassLoader cl,
                                     EventInvoker<T> initInvoker,
                                     EventInvoker<T> destroyInvoker,
                                     Injectable[] attributes,
                                     Injector<T>[] injectors) {
        this.instance = instance;
        this.reinjectable = reinjectable;
        this.cl = cl;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.attributes = attributes;
        this.started = false;
        this.injectors = injectors;
        if (reinjectable) {
            this.updatedInjectors = new HashSet<Injector<T>>();
        } else {
            this.updatedInjectors = null;
        }
    }

    public T getInstance() {
        assert started;
        return instance;
    }

    public boolean isStarted() {
        return started;
    }

    public void start(WorkContext context) throws InstanceInitializationException {
        assert !started;
        if (initInvoker != null) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            WorkContext oldWorkContext = WorkContextTunnel.getThreadWorkContext();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                WorkContextTunnel.setThreadWorkContext(context);
                initInvoker.invokeEvent(instance);
            } catch (ObjectCallbackException e) {
                throw new InstanceInitializationException(e.getMessage(), e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
                WorkContextTunnel.setThreadWorkContext(oldWorkContext);
            }
        }
        started = true;
    }


    public void stop(WorkContext context) throws InstanceDestructionException {
        assert started;
        WorkContext oldWorkContext = WorkContextTunnel.getThreadWorkContext();
        try {
            if (destroyInvoker != null) {
                ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(cl);
                    WorkContextTunnel.setThreadWorkContext(context);
                    destroyInvoker.invokeEvent(instance);
                } finally {
                    Thread.currentThread().setContextClassLoader(oldCl);
                    WorkContextTunnel.setThreadWorkContext(oldWorkContext);
                }
            }
        } catch (ObjectCallbackException e) {
            throw new InstanceDestructionException(e.getMessage(), e);
        } finally {
            started = false;
        }
    }

    public void reinject() throws InstanceLifecycleException {
        if (!reinjectable) {
            throw new IllegalStateException("Implementation is not reinjectable");
        }
        try {
            for (Injector<T> injector : updatedInjectors) {
                injector.inject(instance);
            }
            updatedInjectors.clear();
        } catch (ObjectCreationException ex) {
            throw new InstanceLifecycleException("Unable to inject", ex);
        }
    }

    public void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key) {
        if (instance != null && !reinjectable) {
            throw new IllegalStateException("Implementation is not reinjectable");
        }
        for (int i = 0; i < attributes.length; i++) {
            Injectable attribute = attributes[i];
            if (attribute.getName().equals(referenceName)) {
                Injector<T> injector = injectors[i];
                injector.setObjectFactory(factory, key);
                if (instance != null) {
                    updatedInjectors.add(injector);
                }
            }
        }
    }

    public void removeObjectFactory(String referenceName) {
        if (instance != null && !reinjectable) {
            throw new IllegalStateException("Implementation is not reinjectable");
        }
        for (int i = 0; i < attributes.length; i++) {
            Injectable attribute = attributes[i];
            if (attribute.getName().equals(referenceName)) {
                Injector<T> injector = injectors[i];
                injector.clearObjectFactory();
            }
        }

    }
}
