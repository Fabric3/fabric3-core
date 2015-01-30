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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.AutowireInstantiator;
import org.fabric3.fabric.domain.instantiator.ChannelInstantiator;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.domain.instantiator.WireInstantiator;
import org.fabric3.fabric.domain.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class InstantiationTestCase extends TestCase {
    public static final URI PARENT_URI = URI.create("fabric3://domain");
    public static final String CHILD_URI = PARENT_URI + "/child";

    private LogicalModelInstantiator logicalModelInstantiator;
    private LogicalCompositeComponent parent;

    public void testInstantiateChildren() throws Exception {
        logicalModelInstantiator.include(createParentWithChild(), parent);
        LogicalComponent logicalComponent = (LogicalComponent) parent.getComponents().iterator().next();
        assertEquals(CHILD_URI, logicalComponent.getUri().toString());
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

        logicalModelInstantiator = new LogicalModelInstantiatorImpl(compositeInstantiator,
                                                                    atomicInstantiator,
                                                                    wireInstantiator,
                                                                    autowireInstantiator,
                                                                    channelInstantiator);
        parent = new LogicalCompositeComponent(PARENT_URI, null, null);
    }

    private Composite createParentWithChild() {
        InjectingComponentType childType = new InjectingComponentType();
        MockImplementation childImp = new MockImplementation();
        childImp.setComponentType(childType);
        ComponentDefinition<MockImplementation> child = new ComponentDefinition<>("child");
        child.setImplementation(childImp);

        Composite composite = new Composite(null);
        composite.add(child);
        return composite;

    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = 4128780797281194069L;

        public String getType() {
            return null;
        }
    }

}
