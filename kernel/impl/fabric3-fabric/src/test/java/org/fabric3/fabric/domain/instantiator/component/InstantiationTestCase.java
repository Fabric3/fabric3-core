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
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.AutowireInstantiator;
import org.fabric3.fabric.domain.instantiator.AutowireNormalizer;
import org.fabric3.fabric.domain.instantiator.ChannelInstantiator;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.PromotionNormalizer;
import org.fabric3.fabric.domain.instantiator.PromotionResolutionService;
import org.fabric3.fabric.domain.instantiator.WireInstantiator;
import org.fabric3.fabric.domain.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.CompositeReference;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 *
 */
public class InstantiationTestCase extends TestCase {
    public static final URI PARENT_URI = URI.create("fabric3://domain/parent");
    public static final URI COMPONENT_BASE = URI.create("fabric3://domain/parent/component");
    public static final String COMPONENT_URI = PARENT_URI.toString() + "/component";
    public static final String CHILD_URI = COMPONENT_URI + "/child";
    public static final String SERVICE_URI = COMPONENT_URI + "#service";
    public static final String REFERENCE_URI = COMPONENT_URI + "#reference";

    private LogicalModelInstantiator logicalModelInstantiator;
    private LogicalCompositeComponent parent;

    public void testInstantiateChildren() throws Exception {
        ComponentDefinition<?> definition = createParentWithChild();
        Composite composite = new Composite(null);
        composite.add(definition);
        logicalModelInstantiator.include(composite, parent);
        LogicalCompositeComponent logicalComponent = (LogicalCompositeComponent) parent.getComponents().iterator().next();
        assertEquals(COMPONENT_URI, logicalComponent.getUri().toString());
        LogicalComponent<?> logicalChild = logicalComponent.getComponent(URI.create(CHILD_URI));
        assertEquals(CHILD_URI, logicalChild.getUri().toString());
    }

    public void testInstantiateServiceReference() throws Exception {
        ComponentDefinition<?> definition = createParentWithServiceAndReference();
        Composite composite = new Composite(null);
        composite.add(definition);
        logicalModelInstantiator.include(composite, parent);
        LogicalCompositeComponent logicalComponent = (LogicalCompositeComponent) parent.getComponents().iterator().next();
        LogicalService logicalService = logicalComponent.getService("service");
        assertEquals(SERVICE_URI, logicalService.getUri().toString());
        LogicalReference logicalReference = logicalComponent.getReference("reference");
        assertEquals(REFERENCE_URI, logicalReference.getUri().toString());
    }


    protected void setUp() throws Exception {
        super.setUp();

        ChannelInstantiator channelInstantiator = EasyMock.createMock(ChannelInstantiator.class);
        AtomicComponentInstantiator atomicInstantiator = new AtomicComponentInstantiatorImpl();
        WireInstantiator wireInstantiator = new WireInstantiatorImpl(null);
        CompositeComponentInstantiatorImpl compositeInstantiator = new CompositeComponentInstantiatorImpl(atomicInstantiator,
                                                                                                          wireInstantiator,
                                                                                                          channelInstantiator);
        AutowireInstantiator autowireInstantiator = EasyMock.createMock(AutowireInstantiator.class);
        PromotionResolutionService promotionResolutionService = EasyMock.createMock(PromotionResolutionService.class);
        PromotionNormalizer normalizer = EasyMock.createMock(PromotionNormalizer.class);
        AutowireNormalizer autowireNormalizer = EasyMock.createMock(AutowireNormalizer.class);

        logicalModelInstantiator = new LogicalModelInstantiatorImpl(compositeInstantiator,
                                                                    atomicInstantiator,
                                                                    wireInstantiator,
                                                                    autowireInstantiator,
                                                                    channelInstantiator,
                                                                    normalizer,
                                                                    autowireNormalizer,
                                                                    promotionResolutionService);
        parent = new LogicalCompositeComponent(PARENT_URI, null, null);
    }

    private ComponentDefinition<?> createParentWithChild() {
        InjectingComponentType childType = new InjectingComponentType();
        MockImplementation childImp = new MockImplementation();
        childImp.setComponentType(childType);
        ComponentDefinition<MockImplementation> child =
                new ComponentDefinition<>("child");
        child.setImplementation(childImp);

        Composite type = new Composite(null);
        type.add(child);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private ComponentDefinition<?> createParentWithServiceAndReference() {
        CompositeService service = new CompositeService("service");
        List<URI> references = Collections.emptyList();
        CompositeReference reference = new CompositeReference("reference", references, Multiplicity.ONE_ONE);
        Composite type = new Composite(null);
        type.add(service);
        type.add(reference);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = 4128780797281194069L;

        public QName getType() {
            return null;
        }
    }

}
