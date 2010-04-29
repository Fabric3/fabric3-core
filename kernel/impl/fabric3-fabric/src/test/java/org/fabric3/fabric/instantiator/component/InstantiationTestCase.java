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
*/
package org.fabric3.fabric.instantiator.component;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.AutowireInstantiator;
import org.fabric3.fabric.instantiator.AutowireNormalizer;
import org.fabric3.fabric.instantiator.ChannelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.instantiator.wire.WireInstantiatorImpl;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev$ $Date$
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
        ComponentType childType = new ComponentType();
        MockImplementation childImp = new MockImplementation();
        childImp.setComponentType(childType);
        ComponentDefinition<MockImplementation> child =
                new ComponentDefinition<MockImplementation>("child");
        child.setImplementation(childImp);

        Composite type = new Composite(null);
        type.add(child);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private ComponentDefinition<?> createParentWithServiceAndReference() {
        CompositeService service = new CompositeService("service", null, null);
        List<URI> references = Collections.emptyList();
        CompositeReference reference = new CompositeReference("reference", references, Multiplicity.ONE_ONE);
        Composite type = new Composite(null);
        type.add(service);
        type.add(reference);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private class MockImplementation extends Implementation<AbstractComponentType<?, ?, ?, ?>> {
        private static final long serialVersionUID = 4128780797281194069L;

        public QName getType() {
            return null;
        }
    }

}
