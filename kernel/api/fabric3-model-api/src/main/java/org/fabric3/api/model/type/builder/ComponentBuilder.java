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
package org.fabric3.api.model.type.builder;

import org.fabric3.api.model.type.F3NamespaceContext;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.component.Target;

/**
 * Base builder for {@link Component}s.
 */
public abstract class ComponentBuilder<T extends ComponentBuilder> extends AbstractBuilder {

    /**
     * Adds a binding configuration to a service provided by the component.
     *
     * @param serviceName       the service name
     * @param bindingDefinition the binding definition
     * @return the builder
     */
    public T binding(String serviceName, Binding bindingDefinition) {
        checkState();
        Component<?> definition = getComponent();
        Service<Component> service = definition.getServices().get(serviceName);
        if (service == null) {
            service = new Service<>(serviceName);
            definition.add(service);
        }
        service.addBinding(bindingDefinition);
        return builder();
    }

    /**
     * Adds a reference with the given name and target.
     *
     * @param name   the reference name
     * @param target the target
     */
    public T reference(String name, String target) {
        checkState();
        Component<?> definition = getComponent();
        Reference<Component> reference = new Reference<>(name, Multiplicity.ONE_ONE);
        reference.addTarget(new Target(target));
        definition.add(reference);
        return builder();
    }

    /**
     * Adds a reference with the given name and target.
     *
     * @param name   the reference name
     * @param target the target
     */
    public T reference(String name, String target, boolean required) {
        checkState();
        Component<?> definition = getComponent();
        Multiplicity multiplicity = required ? Multiplicity.ONE_ONE : Multiplicity.ZERO_ONE;
        Reference<Component> reference = new Reference<>(name, multiplicity);
        reference.addTarget(new Target(target));
        definition.add(reference);
        return builder();
    }

    /**
     * Adds a reference with the given name and target.
     *
     * @param name         the reference name
     * @param target       the target
     * @param multiplicity the    multiplicity
     */
    public T reference(String name, String target, Multiplicity multiplicity) {
        checkState();
        Component<?> definition = getComponent();
        Reference<Component> reference = new Reference<>(name, multiplicity);
        reference.addTarget(new Target(target));
        definition.add(reference);
        return builder();
    }

    /**
     * Adds a property value.
     *
     * @param name  the property name
     * @param value the value
     * @return the builder
     */
    public T property(String name, Object value) {
        checkState();
        PropertyValue propertyValue = new PropertyValue(name, value);
        getComponent().add(propertyValue);
        return builder();
    }

    /**
     * Adds a property value sourced from the XPath expression.
     *
     * @param name  the property name
     * @param xpath the XPath expression
     * @return the builder
     */
    public T propertyExpression(String name, String xpath) {
        checkState();
        PropertyValue propertyValue = new PropertyValue(name, xpath);
        propertyValue.setNamespaceContext(new F3NamespaceContext());
        getComponent().add(propertyValue);
        return builder();
    }

    /**
     * Sets the wire key for a component for use with Map-based reference.
     *
     * @param key the key
     * @return the builder
     */
    public T key(Object key) {
        checkState();
        getComponent().setKey(key.toString());
        return builder();
    }

    /**
     * Sets the wire order for a component for use with multiplicity reference.
     *
     * @param order the order
     * @return the builder
     */
    public T order(int order) {
        checkState();
        getComponent().setOrder(order);
        return builder();
    }

    protected abstract Component<?> getComponent();

    @SuppressWarnings("unchecked")
    private T builder() {
        return (T) this;
    }

}
