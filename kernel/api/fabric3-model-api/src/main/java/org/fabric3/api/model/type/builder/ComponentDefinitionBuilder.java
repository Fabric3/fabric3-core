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
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.component.Target;

/**
 * Base builder for {@link ComponentDefinition}s.
 */
public abstract class ComponentDefinitionBuilder<T extends ComponentDefinitionBuilder> extends AbstractBuilder {

    /**
     * Adds a binding configuration to a service provided by the component.
     *
     * @param serviceName       the service name
     * @param bindingDefinition the binding definition
     * @return the builder
     */
    public T binding(String serviceName, BindingDefinition bindingDefinition) {
        checkState();
        ComponentDefinition<?> definition = getDefinition();
        ServiceDefinition<ComponentDefinition> service = definition.getServices().get(serviceName);
        if (service == null) {
            service = new ServiceDefinition<>(serviceName);
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
        ComponentDefinition<?> definition = getDefinition();
        ReferenceDefinition<ComponentDefinition> reference = new ReferenceDefinition<>(name, Multiplicity.ONE_ONE);
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
        ComponentDefinition<?> definition = getDefinition();
        Multiplicity multiplicity = required ? Multiplicity.ONE_ONE : Multiplicity.ZERO_ONE;
        ReferenceDefinition<ComponentDefinition> reference = new ReferenceDefinition<>(name, multiplicity);
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
        ComponentDefinition<?> definition = getDefinition();
        ReferenceDefinition<ComponentDefinition> reference = new ReferenceDefinition<>(name, multiplicity);
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
        getDefinition().add(propertyValue);
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
        getDefinition().add(propertyValue);
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
        getDefinition().setKey(key.toString());
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
        getDefinition().setOrder(order);
        return builder();
    }

    protected abstract ComponentDefinition<?> getDefinition();

    @SuppressWarnings("unchecked")
    private T builder() {
        return (T) this;
    }

}
