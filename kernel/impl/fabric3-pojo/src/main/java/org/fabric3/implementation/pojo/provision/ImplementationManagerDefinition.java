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
package org.fabric3.implementation.pojo.provision;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectionSite;

/**
 * Base class for implementation manager factory definitions.
 */
public class ImplementationManagerDefinition {
    private Class<?>  implementationClass;
    private Constructor<?> constructor;
    private Method initMethod;
    private Method destroyMethod;
    private boolean reinjectable;
    private Map<InjectionSite, Injectable> construction = new HashMap<>();
    private Map<InjectionSite, Injectable> postConstruction = new HashMap<>();
    private Map<InjectionSite, Injectable> reinjection = new HashMap<>();

    /**
     * Returns the signature of the constructor that should be used.
     *
     * @return the constructor that should be used
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * Sets the signature of the constructor that should be used.
     *
     * @param constructor the constructor that should be used
     */
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * Gets the init method.
     *
     * @return the signature for the init method
     */
    public Method getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the init method.
     *
     * @param initMethod the signature of the init method
     */
    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Gets the destroy method.
     *
     * @return the signature of the destroy method
     */
    public Method getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the destroy method.
     *
     * @param destroyMethod the signature of the destroy method
     */
    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    /**
     * Gets the implementation class.
     *
     * @return Implementation class.
     */
    public Class<?>  getImplementationClass() {
        return implementationClass;
    }

    /**
     * Sets the implementation class.
     *
     * @param clazz Implementation class.
     */
    public void setImplementationClass(Class<?>  clazz) {
        this.implementationClass = clazz;
    }

    /**
     * Returns the map of injections to be performed during construction.
     *
     * @return the map of injections to be performed during construction
     */
    public Map<InjectionSite, Injectable> getConstruction() {
        return construction;
    }

    /**
     * Returns the map of injections to be performed after construction.
     *
     * @return the map of injections to be performed after construction
     */
    public Map<InjectionSite, Injectable> getPostConstruction() {
        return postConstruction;
    }

    /**
     * Returns the map of injections to be performed during reinjection.
     *
     * @return the map of injections to be performed during reinjection
     */
    public Map<InjectionSite, Injectable> getReinjectables() {
        return reinjection;
    }

    /**
     * Returns true if the implementation is reinjectable, e.g. it is composite-scoped.
     *
     * @return true if the implementation is reinjectable, e.g. it is composite-scoped
     */
    public boolean isReinjectable() {
        return reinjectable;
    }

    /**
     * Sets if the implementation is reinjectable.
     *
     * @param reinjectable true if the implementation is reinjectable
     */
    public void setReinjectable(boolean reinjectable) {
        this.reinjectable = reinjectable;
    }

}
