/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.fabric.deployment.instantiator.component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentConsumer;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentProducer;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyMany;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.component.Target;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 *
 */
public class AtomicComponentInstantiatorTestCase extends TestCase {
    private static final URI PRODUCER_TARGET = URI.create("producerTarget");
    private static final URI CONSUMER_SOURCE = URI.create("consumerTarget");
    private static final String REFERENCE_TARGET = "referenceTarget";

    private AtomicComponentInstantiatorImpl instantiator;
    private LogicalCompositeComponent parent;
    private InstantiationContext context;
    private ComponentDefinition<MockImplementation> component;

    public void testInstantiate() throws Exception {
        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);
        assertEquals("parent/component", logicalComponent.getUri().toString());

        LogicalService logicalService = logicalComponent.getService("service");
        assertEquals("parent/component#service", logicalService.getUri().toString());
        assertFalse(logicalService.getBindings().isEmpty());
        assertFalse(logicalService.getCallbackBindings().isEmpty());

        LogicalReference logicalReference = logicalComponent.getReference("reference");
        assertEquals("parent/component#reference", logicalReference.getUri().toString());
        assertFalse(logicalReference.getBindings().isEmpty());
        assertFalse(logicalReference.getCallbackBindings().isEmpty());

        LogicalConsumer logicalConsumer = logicalComponent.getConsumer("consumer");
        assertEquals("parent/component#consumer", logicalConsumer.getUri().toString());

        LogicalProducer logicalProducer = logicalComponent.getProducer("producer");
        assertEquals("parent/component#producer", logicalProducer.getUri().toString());

        LogicalResourceReference logicalResourceReference = logicalComponent.getResourceReference("resource");
        assertEquals("parent/component#resource", logicalResourceReference.getUri().toString());
    }

    public void testDuplicateComponent() throws Exception {
        parent.addComponent(new LogicalComponent<MockImplementation>(URI.create("parent/component"), null, parent));
        instantiator.instantiate(component, parent, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof DuplicateComponent);
    }

    public void testPropertiesDefaultValue() {
        Property definition = new Property("property");
        Document document = EasyMock.createMock(Document.class);
        definition.setDefaultValue(document);
        component.getComponentType().add(definition);

        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);
        LogicalProperty logicalProperty = logicalComponent.getProperties("property");
        assertNotNull(logicalProperty.getValue());
    }

    public void testPropertyValueOnComponentDefinition() {
        Property definition = new Property("property");
        component.getComponentType().add(definition);

        Document document = EasyMock.createMock(Document.class);
        PropertyValue value = new PropertyValue("property", document, PropertyMany.SINGLE);
        component.add(value);
        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);
        LogicalProperty logicalProperty = logicalComponent.getProperties("property");
        assertNotNull(logicalProperty.getValue());
    }

    public void testPropertyValueXPathSource() throws Exception {
        Property definition = new Property("property");
        component.getComponentType().add(definition);

        PropertyValue value = new PropertyValue("property", "$theProp");
        NamespaceContext namespaceContext = EasyMock.createMock(NamespaceContext.class);
        value.setNamespaceContext(namespaceContext);
        component.add(value);

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("values");
        document.appendChild(root);
        Element valElement = document.createElement("value");
        valElement.setTextContent("test");
        root.appendChild(valElement);
        parent.setProperties(new LogicalProperty("theProp", document, false, parent));

        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);

        LogicalProperty logicalProperty = logicalComponent.getProperties("property");
        Element rootReturned = logicalProperty.getValue().getDocumentElement();
        assertEquals("values", rootReturned.getNodeName());
        assertEquals("value", rootReturned.getChildNodes().item(0).getNodeName());
    }

    public void testPropertyValueFileSource() throws Exception {
        createFileSourceProperty("property.xml");

        parent.setProperties(new LogicalProperty("theProp", null, false, parent));
        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);
        LogicalProperty logicalProperty = logicalComponent.getProperties("property");
        Element rootReturned = logicalProperty.getValue().getDocumentElement();
        assertEquals("values", rootReturned.getNodeName());
        assertEquals("value", rootReturned.getChildNodes().item(0).getNodeName());
    }

    public void testPropertyValueNoValuesElementFileSource() throws Exception {
        createFileSourceProperty("propertyNoValues.xml");

        parent.setProperties(new LogicalProperty("theProp", null, false, parent));
        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);
        LogicalProperty logicalProperty = logicalComponent.getProperties("property");
        Element rootReturned = logicalProperty.getValue().getDocumentElement();
        assertEquals("values", rootReturned.getNodeName());
        assertEquals("value", rootReturned.getChildNodes().item(0).getNodeName());
    }

    public void testPropertyValueNoRootElementFileSource() throws Exception {
        createFileSourceProperty("propertyNoRoot.xml");

        parent.setProperties(new LogicalProperty("theProp", null, false, parent));
        LogicalComponent logicalComponent = instantiator.instantiate(component, parent, context);
        LogicalProperty logicalProperty = logicalComponent.getProperties("property");
        Element rootReturned = logicalProperty.getValue().getDocumentElement();
        assertEquals("values", rootReturned.getNodeName());
        assertEquals("value", rootReturned.getChildNodes().item(0).getNodeName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComponentType type = new ComponentType();
        MockImplementation implementation = new MockImplementation();
        implementation.setComponentType(type);
        component = new ComponentDefinition<MockImplementation>("component");
        component.setImplementation(implementation);

        createService(component);
        createReference(component);
        createProducer(component);
        createConsumer(component);
        createResource(component);

        URI parentUri = URI.create("parent");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("parent");
        parent = new LogicalCompositeComponent(parentUri, definition, null);

        context = new InstantiationContext();
        instantiator = new AtomicComponentInstantiatorImpl();

    }

    private void createConsumer(ComponentDefinition component) {
        ConsumerDefinition definition = new ConsumerDefinition("consumer");
        component.getComponentType().add(definition);

        ComponentConsumer consumer = new ComponentConsumer("consumer", Collections.singletonList(CONSUMER_SOURCE));
        component.add(consumer);
    }

    private void createProducer(ComponentDefinition component) {
        ProducerDefinition definition = new ProducerDefinition("producer");
        component.getComponentType().add(definition);

        ComponentProducer producer = new ComponentProducer("producer", Collections.singletonList(PRODUCER_TARGET));
        component.add(producer);
    }

    private void createReference(ComponentDefinition component) {
        ReferenceDefinition definition = new ReferenceDefinition("reference", Multiplicity.ONE_ONE);
        component.getComponentType().add(definition);

        ComponentReference reference = new ComponentReference("reference", Multiplicity.ONE_ONE);
        Target target = new Target(REFERENCE_TARGET);
        reference.addTarget(target);
        reference.addBinding(new MockBinding());
        reference.addCallbackBinding(new MockBinding());
        component.add(reference);
    }

    private void createService(ComponentDefinition component) {
        ServiceDefinition definition = new ServiceDefinition("service");
        component.getComponentType().add(definition);

        ComponentService service = new ComponentService("service");
        service.addBinding(new MockBinding());
        service.addCallbackBinding(new MockBinding());
        component.add(service);
    }

    private void createResource(ComponentDefinition component) {
        ResourceReferenceDefinition definition = new ResourceReferenceDefinition("resource", null, false);
        component.getComponentType().add(definition);
    }

    private void createFileSourceProperty(String fileName) throws URISyntaxException {
        Property definition = new Property("property");
        component.getComponentType().add(definition);
        String name = getClass().getPackage().getName().replace(".", "/") + "/" + fileName;
        URI fileSource = getClass().getClassLoader().getResource(name).toURI();
        PropertyValue value = new PropertyValue("property", fileSource);
        NamespaceContext namespaceContext = EasyMock.createMock(NamespaceContext.class);
        value.setNamespaceContext(namespaceContext);
        component.add(value);
    }


    private class MockBinding extends BindingDefinition {
        private static final long serialVersionUID = -7088192438672216044L;

        public MockBinding() {
            super(null, null);
        }
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = 820191204264624733L;

        @Override
        public QName getType() {
            return null;
        }
    }

}
