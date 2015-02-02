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
package org.fabric3.api.model.type.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Callback;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.ResourceReference;

/**
 * A component type associated with an implementation that supports injection.
 */
public class InjectingComponentType extends ComponentType {
    private String implClass;
    private String scope;
    private int initLevel;
    private boolean managed;
    private ManagementInfo managementInfo;

    private Constructor<?> constructor;
    private Method initMethod;
    private Method destroyMethod;
    private Map<InjectionSite, Injectable> injectionSites = new HashMap<>();
    private Map<ModelObject, InjectionSite> injectionSiteMapping = new HashMap<>();
    private Map<String, Callback> callbacks = new HashMap<>();
    private Map<String, Signature> consumerSignatures = new HashMap<>();

    /**
     * Constructor.
     *
     * @param implClass the class this component type represents.
     */
    public InjectingComponentType(String implClass) {
        this.implClass = implClass;
    }

    /**
     * Default constructor. Used primarily for testing.
     */
    public InjectingComponentType() {
    }

    /**
     * Returns the java class name for this component type.
     *
     * @return the the java class name for this component type
     */
    public String getImplClass() {
        return implClass;
    }

    /**
     * Returns the lifecycle scope for the component.
     *
     * @return the lifecycle scope for the component
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the lifecycle scope for the component.
     *
     * @param scope the lifecycle scope for the component
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Sets the default initialization level for components of this type. A value greater than zero indicates that components should be eagerly initialized.
     *
     * @param initLevel default initialization level for components of this type
     */
    public void setInitLevel(int initLevel) {
        this.initLevel = initLevel;
    }

    /**
     * Returns true if this component should be eagerly initialized.
     *
     * @return true if this component should be eagerly initialized
     */
    public boolean isEagerInit() {
        return initLevel > 0;
    }

    /**
     * Returns true if this implementation is to be exposed for management.
     *
     * @return true if this implementation is to be exposed for management
     */
    public boolean isManaged() {
        return managed;
    }

    /**
     * Sets if this implementation is to be exposed for management.
     *
     * @param managed true if this implementation is to be exposed for management
     */
    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    /**
     * Returns the ManagementInfo if this implementation is managed or null.
     *
     * @return the ManagementInfo or null
     */
    public ManagementInfo getManagementInfo() {
        return managementInfo;
    }

    /**
     * Sets the ManagementInfo for this implementation.
     *
     * @param managementInfo the ManagementInfo
     */
    public void setManagementInfo(ManagementInfo managementInfo) {
        managementInfo.setParent(this);
        this.managementInfo = managementInfo;
    }

    /**
     * Add a reference and its associated with an injection site.
     *
     * @param reference     the reference to add
     * @param injectionSite the injection site for the reference
     */
    public void add(Reference<ComponentType> reference, InjectionSite injectionSite) {
        super.add(reference);
        Injectable injectable = new Injectable(InjectableType.REFERENCE, reference.getName());
        addInjectionSite(injectionSite, injectable);
        injectionSiteMapping.put(reference, injectionSite);
    }

    /**
     * Add a producer and its associated injection site.
     *
     * @param producer      the producer to add
     * @param injectionSite the injection site for the producer
     */
    public void add(Producer<ComponentType> producer, InjectionSite injectionSite) {
        super.add(producer);
        Injectable injectable = new Injectable(InjectableType.PRODUCER, producer.getName());
        addInjectionSite(injectionSite, injectable);
        injectionSiteMapping.put(producer, injectionSite);
    }

    /**
     * Add a consumer and its associated method signature.
     *
     * @param consumer  the consumer to add
     * @param signature the consumer method signature
     */
    public void add(Consumer<ComponentType> consumer, Signature signature) {
        super.add(consumer);
        consumerSignatures.put(consumer.getName(), signature);
    }

    /**
     * Returns the consumer method signature for the given consumer name
     *
     * @param name the consumer  name
     * @return the method signature
     */
    public Signature getConsumerSignature(String name) {
        return consumerSignatures.get(name);
    }

    /**
     * Add a property and its associated with an injection site.
     *
     * @param property      the property to add
     * @param injectionSite the injection site for the property
     */
    public void add(Property property, InjectionSite injectionSite) {
        super.add(property);
        Injectable injectable = new Injectable(InjectableType.PROPERTY, property.getName());
        addInjectionSite(injectionSite, injectable);
        injectionSiteMapping.put(property, injectionSite);
    }

    /**
     * Add a resource reference and its associated an injection site.
     *
     * @param definition    the resource reference to add
     * @param injectionSite the injection site for the resource
     */
    public void add(ResourceReference definition, InjectionSite injectionSite) {
        super.add(definition);
        Injectable injectable = new Injectable(InjectableType.RESOURCE, definition.getName());
        addInjectionSite(injectionSite, injectable);
        injectionSiteMapping.put(definition, injectionSite);
    }

    /**
     * Adds a callback proxy definition and its associated injection site
     *
     * @param definition    the callback proxy definition
     * @param injectionSite the proxy injection site
     */
    public void add(Callback definition, InjectionSite injectionSite) {
        definition.setParent(this);
        String name = definition.getName();
        callbacks.put(name, definition);
        Injectable injectable = new Injectable(InjectableType.CALLBACK, name);
        addInjectionSite(injectionSite, injectable);
        injectionSiteMapping.put(definition, injectionSite);
    }

    /**
     * Returns a collection of defined callback proxy definitions keyed by name
     *
     * @return the collection of proxy definitions
     */
    public Map<String, Callback> getCallbacks() {
        return callbacks;
    }

    /**
     * Add the injection site for an injectable value.
     *
     * @param site   the injection site
     * @param source the value to be injected
     */
    public void addInjectionSite(InjectionSite site, Injectable source) {
        site.setParent(this);
        source.setParent(this);
        injectionSites.put(site, source);
    }

    /**
     * Returns the map of all injection mappings.
     *
     * @return the map of all injection mappings
     */
    public Map<InjectionSite, Injectable> getInjectionSites() {
        return injectionSites;
    }

    /**
     * Returns the injection site for the model object.
     *
     * @param object the model object, e.g. a reference, producer, callback, etc.
     * @return the injection site or null
     */
    public InjectionSite getInjectionSite(ModelObject object) {
        return injectionSiteMapping.get(object);
    }

    /**
     * Returns the model object-to-injection site mappings.
     *
     * @return the mappings
     */
    public Map<ModelObject, InjectionSite> getInjectionSiteMappings() {
        return injectionSiteMapping;
    }

    /**
     * Returns the signature of the constructor to use.
     *
     * @return the constructor to use
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * Sets the signature of the constructor to use.
     *
     * @param constructor the constructor to use
     */
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * Returns the component initializer method.
     *
     * @return the component initializer method
     */
    public Method getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the component initializer method.
     *
     * @param initMethod the component initializer method
     */
    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Returns the component destructor method.
     *
     * @return the component destructor method
     */
    public Method getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the component destructor method.
     *
     * @param destroyMethod the component destructor method
     */
    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

}
