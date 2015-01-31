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
 */
package org.fabric3.implementation.java.runtime;

import java.lang.reflect.Type;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.InjectionAttributes;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;

/**
 * A factory that enabled non-managed code to be treated as a Java implementation instance.
 */
public class NonManagedImplementationManagerFactory implements ImplementationManagerFactory, ImplementationManager {
    private Object instance;
    private SingletonObjectFactory<?> factory;

    public NonManagedImplementationManagerFactory(Object instance) {
        this.instance = instance;
        factory = new SingletonObjectFactory<>(instance);
    }

    public ImplementationManager createManager() {
        return this;
    }

    public Class<?> getImplementationClass() {
        return instance.getClass();
    }

    public void startUpdate() {
    }

    public void endUpdate() {
    }

    public ObjectFactory<?> getObjectFactory(Injectable attribute) {
        return factory;
    }

    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory) {
    }

    public void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, InjectionAttributes attributes) {
    }

    public void removeObjectFactory(Injectable injectable) {
    }

    public Class<?> getMemberType(Injectable injectable) {
        throw new UnsupportedOperationException();
    }

    public Type getGenericType(Injectable injectable) {
        throw new UnsupportedOperationException();
    }

    public Object newInstance() throws ContainerException {
        return instance;
    }

    public void start(Object instance) throws ContainerException {
    }

    public void stop(Object instance) throws ContainerException {
    }

    public void reinject(Object instance) throws ContainerException {
    }

    public void updated(Object instance, String referenceName) {
    }

    public void removed(Object instance, String referenceName) {
    }
}
