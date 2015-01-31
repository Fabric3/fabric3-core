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
package org.fabric3.implementation.pojo.manager;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public class ImplementationManagerImpl implements ImplementationManager {
    private URI componentUri;
    private final ObjectFactory<?> constructor;
    private Injectable[] injectables;
    private final Injector<Object>[] injectors;
    private final LifecycleInvoker initInvoker;
    private final LifecycleInvoker destroyInvoker;
    private final ClassLoader cl;
    private final boolean reinjectable;
    private Set<Injector<Object>> updatedInjectors;

    public ImplementationManagerImpl(URI componentUri,
                                     ObjectFactory<?> constructor,
                                     Injectable[] injectables,
                                     Injector<Object>[] injectors,
                                     LifecycleInvoker initInvoker,
                                     LifecycleInvoker destroyInvoker,
                                     boolean reinjectable,
                                     ClassLoader cl) {
        this.componentUri = componentUri;
        this.constructor = constructor;
        this.injectables = injectables;
        this.injectors = injectors;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.reinjectable = reinjectable;
        this.cl = cl;
        if (reinjectable) {
            this.updatedInjectors = new HashSet<>();
        } else {
            this.updatedInjectors = null;
        }
    }

    public Object newInstance() throws ContainerException {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        try {
            // FABRIC-40: if a component invokes services from a setter, the message content will be over-written. Save so it can be restored after instance
            // injection.
            Message message = MessageCache.getMessage();
            Object content = message.getBody();
            Object instance = constructor.getInstance();
            if (injectors != null) {
                for (Injector<Object> injector : injectors) {
                    injector.inject(instance);
                }
            }
            // restore the original contents
            message.setBody(content);
            return instance;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void start(Object instance) throws ContainerException {
        if (initInvoker != null) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                initInvoker.invoke(instance);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
    }

    public void stop(Object instance) throws ContainerException {
        if (destroyInvoker != null) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                destroyInvoker.invoke(instance);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
    }

    public void reinject(Object instance) throws ContainerException {
        if (!reinjectable) {
            throw new IllegalStateException("Implementation is not reinjectable:" + componentUri);
        }
        for (Injector<Object> injector : updatedInjectors) {
            injector.inject(instance);
        }
        updatedInjectors.clear();
    }

    public void updated(Object instance, String referenceName) {
        if (instance != null && !reinjectable) {
            throw new IllegalStateException("Implementation is not reinjectable: " + componentUri);
        }
        for (int i = 0; i < injectables.length; i++) {
            Injectable attribute = injectables[i];
            if (attribute.getName().equals(referenceName)) {
                Injector<Object> injector = injectors[i];
                if (instance != null) {
                    updatedInjectors.add(injector);
                }
            }
        }
    }

    public void removed(Object instance, String referenceName) {
        if (instance != null && !reinjectable) {
            throw new IllegalStateException("Implementation is not reinjectable: " + componentUri);
        }
        for (int i = 0; i < injectables.length; i++) {
            Injectable attribute = injectables[i];
            if (attribute.getName().equals(referenceName)) {
                Injector<Object> injector = injectors[i];
                injector.clearObjectFactory();
                if (instance != null) {
                    updatedInjectors.add(injector);
                }
            }
        }

    }

}
