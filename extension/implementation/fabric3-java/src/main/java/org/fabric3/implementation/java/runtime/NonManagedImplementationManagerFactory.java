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
import java.util.function.Supplier;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.implementation.pojo.manager.ImplementationManager;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.injection.InjectionAttributes;

/**
 * A factory that enabled non-managed code to be treated as a Java implementation instance.
 */
public class NonManagedImplementationManagerFactory implements ImplementationManagerFactory, ImplementationManager {
    private Object instance;
    private Supplier<?> supplier;

    public NonManagedImplementationManagerFactory(Object instance) {
        this.instance = instance;
        supplier = () -> instance;
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

    public Supplier<?> getObjectSupplier(Injectable attribute) {
        return supplier;
    }

    public void setSupplier(Injectable injectable, Supplier<?> supplier) {
    }

    public void setSupplier(Injectable injectable, Supplier<?> supplier, InjectionAttributes attributes) {
    }

    public void removeSupplier(Injectable injectable) {
    }

    public Class<?> getMemberType(Injectable injectable) {
        throw new UnsupportedOperationException();
    }

    public Type getGenericType(Injectable injectable) {
        throw new UnsupportedOperationException();
    }

    public Object newInstance() {
        return instance;
    }

    public void start(Object instance) {
    }

    public void stop(Object instance) {
    }

    public void reinject(Object instance) {
    }

    public void updated(Object instance, String referenceName) {
    }

    public void removed(Object instance, String referenceName) {
    }
}
