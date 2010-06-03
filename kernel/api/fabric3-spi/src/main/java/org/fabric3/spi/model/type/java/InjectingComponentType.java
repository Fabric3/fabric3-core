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
package org.fabric3.spi.model.type.java;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.component.CallbackDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;

/**
 * A component type associated with an implementation that supports injection.
 *
 * @version $Rev$ $Date$
 */
public class InjectingComponentType extends ComponentType {
    private static final long serialVersionUID = -2602867276842414240L;

    private String implClass;
    private String scope;
    private int initLevel;
    private Signature constructor;
    private Signature initMethod;
    private Signature destroyMethod;
    private long maxAge;
    private long maxIdleTime;
    private Map<InjectionSite, Injectable> injectionSites = new HashMap<InjectionSite, Injectable>();
    private Map<String, CallbackDefinition> callbacks = new HashMap<String, CallbackDefinition>();
    private Map<String, Signature> consumerSignatures = new HashMap<String, Signature>();

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
     * Sets the default initialization level for components of this type. A value greater than zero indicates that components should be eagerly
     * initialized.
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
     * Returns the idle time allowed between operations in milliseconds if the implementation is conversational
     *
     * @return the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * Sets the idle time allowed between operations in milliseconds if the implementation is conversational.
     *
     * @param maxIdleTime the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * Returns the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     *
     * @return the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age a conversation may remain active in milliseconds if the implementation is conversational.
     *
     * @param maxAge the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Add a reference and its associated with an injection site.
     *
     * @param reference     the reference to add
     * @param injectionSite the injection site for the reference
     */
    public void add(ReferenceDefinition reference, InjectionSite injectionSite) {
        super.add(reference);
        Injectable injectable = new Injectable(InjectableType.REFERENCE, reference.getName());
        addInjectionSite(injectionSite, injectable);
    }

    /**
     * Add a producer and its associated injection site.
     *
     * @param producer      the producer to add
     * @param injectionSite the injection site for the producer
     */
    public void add(ProducerDefinition producer, InjectionSite injectionSite) {
        super.add(producer);
        Injectable injectable = new Injectable(InjectableType.PRODUCER, producer.getName());
        addInjectionSite(injectionSite, injectable);
    }

    /**
     * Add a consumer and its associated method signature.
     *
     * @param consumer  the consumer to add
     * @param signature the consumer method signature
     */
    public void add(ConsumerDefinition consumer, Signature signature) {
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
    }

    /**
     * Add a resource and its associated with an injection site.
     *
     * @param resource      the resource to add
     * @param injectionSite the injection site for the resource
     */
    public void add(ResourceDefinition resource, InjectionSite injectionSite) {
        super.add(resource);
        Injectable injectable = new Injectable(InjectableType.RESOURCE, resource.getName());
        addInjectionSite(injectionSite, injectable);
    }

    /**
     * Adds a callback proxy defintion and its associated injection site
     *
     * @param definition    the callback proxy definition
     * @param injectionSite the proxy injection site
     */
    public void add(CallbackDefinition definition, InjectionSite injectionSite) {
        String name = definition.getName();
        callbacks.put(name, definition);
        Injectable injectable = new Injectable(InjectableType.CALLBACK, name);
        addInjectionSite(injectionSite, injectable);
    }

    /**
     * Returns a collection of defined callback proxy definitions keyed by name
     *
     * @return the collection of proxy definitions
     */
    public Map<String, CallbackDefinition> getCallbacks() {
        return callbacks;
    }

    /**
     * Add the injection site for an injectable value.
     *
     * @param site   the injection site
     * @param source the value to be injected
     */
    public void addInjectionSite(InjectionSite site, Injectable source) {
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
     * Returns the signature of the constructor to use.
     *
     * @return the signature of the constructor to use
     */
    public Signature getConstructor() {
        return constructor;
    }

    /**
     * Sets the signature of the constructor to use.
     *
     * @param constructor the signature of the constructor to use
     */
    public void setConstructor(Signature constructor) {
        this.constructor = constructor;
    }

    /**
     * Returns the component initializer method.
     *
     * @return the component initializer method
     */
    public Signature getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the component initializer method.
     *
     * @param initMethod the component initializer method
     */
    public void setInitMethod(Signature initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Returns the component destructor method.
     *
     * @return the component destructor method
     */
    public Signature getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the component destructor method.
     *
     * @param destroyMethod the component destructor method
     */
    public void setDestroyMethod(Signature destroyMethod) {
        this.destroyMethod = destroyMethod;
    }


}
