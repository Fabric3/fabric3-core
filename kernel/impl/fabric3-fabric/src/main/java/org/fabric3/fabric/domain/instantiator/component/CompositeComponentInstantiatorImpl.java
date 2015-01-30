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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.instantiator.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.ChannelInstantiator;
import org.fabric3.fabric.domain.instantiator.CompositeComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.WireInstantiator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates a composite component in the logical representation of a domain. Child components will be recursively instantiated if they exist. <p/> Service
 * and reference information configured as part of a <code>&lt;component&gt;</code> entry and component type will be merged into a single logical artifact.
 */
public class CompositeComponentInstantiatorImpl extends AbstractComponentInstantiator implements CompositeComponentInstantiator {

    private AtomicComponentInstantiator atomicInstantiator;
    private WireInstantiator wireInstantiator;
    private ChannelInstantiator channelInstantiator;

    @Constructor
    public CompositeComponentInstantiatorImpl(@Reference AtomicComponentInstantiator atomicInstantiator,
                                              @Reference WireInstantiator wireInstantiator,
                                              @Reference ChannelInstantiator channelInstantiator) {
        this.atomicInstantiator = atomicInstantiator;
        this.wireInstantiator = wireInstantiator;
        this.channelInstantiator = channelInstantiator;
    }

    public CompositeComponentInstantiatorImpl(AtomicComponentInstantiator atomicInstantiator, WireInstantiator wireInstantiator) {
        this(atomicInstantiator, wireInstantiator, null);
    }

    public LogicalComponent<CompositeImplementation> instantiate(ComponentDefinition<CompositeImplementation> definition,
                                                                 LogicalCompositeComponent parent,
                                                                 InstantiationContext context) {

        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        Composite composite = definition.getImplementation().getComponentType();

        LogicalCompositeComponent component = new LogicalCompositeComponent(uri, definition, parent);
        initializeProperties(component, definition, context);
        instantiateChildComponents(component, composite, context);
        wireInstantiator.instantiateCompositeWires(composite, component, context);
        instantiateResources(component, composite);
        wireInstantiator.instantiateCompositeWires(composite, component, context);
        if (channelInstantiator != null) {
            channelInstantiator.instantiateChannels(composite, component, context);
        }
        if (parent.getComponent(uri) != null) {
            DuplicateComponent error = new DuplicateComponent(uri, parent);
            context.addError(error);
        }
        parent.addComponent(component);
        return component;
    }

    @SuppressWarnings({"unchecked"})
    private void instantiateChildComponents(LogicalCompositeComponent component, Composite composite, InstantiationContext context) {

        // create the child components
        List<LogicalComponent<?>> children = new ArrayList<>();
        for (ComponentDefinition<? extends Implementation<?>> child : composite.getComponents().values()) {

            LogicalComponent<?> childComponent = atomicInstantiator.instantiate(child, component, context);
            component.addComponent(childComponent);
            children.add(childComponent);
        }
        // resolve the reference wires after the children have been instantiated and added to the parent, otherwise targets will not resolve
        for (LogicalComponent<?> child : children) {
            wireInstantiator.instantiateReferenceWires(child, context);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void instantiateResources(LogicalCompositeComponent component, Composite composite) {
        for (ResourceDefinition definition : composite.getResources()) {
            LogicalResource<?> resource = new LogicalResource(definition, component);
            component.addResource(resource);
        }
    }

}
