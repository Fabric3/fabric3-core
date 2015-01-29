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
package org.fabric3.fabric.domain.generator.wire;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class PhysicalOperationGeneratorImplTestCase extends TestCase {

    private static final URI CONTRIBUTION_URI = URI.create("contribution");

    public void testGenerateOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        List<LogicalOperation> list = Collections.singletonList(createOperation());

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list);

        assertEquals(1, definitions.size());
        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertEquals("java.lang.String", definition.getSourceParameterTypes().get(0));
        assertEquals("java.lang.String", definition.getSourceReturnType());
        assertEquals("java.lang.Exception", definition.getSourceFaultTypes().get(0));
        assertEquals("java.lang.String", definition.getTargetParameterTypes().get(0));
        assertEquals("java.lang.String", definition.getTargetReturnType());
        assertEquals("java.lang.Exception", definition.getTargetFaultTypes().get(0));
        EasyMock.verify(resolver, registry);
    }

    public void testGenerateRemoteOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        LogicalOperation operation = createOperation();
        operation.getDefinition().setRemotable(true);
        List<LogicalOperation> list = Collections.singletonList(operation);

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list);

        assertEquals(1, definitions.size());
        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertEquals("java.lang.String", definition.getSourceParameterTypes().get(0));
        assertEquals("java.lang.String", definition.getSourceReturnType());
        assertEquals("java.lang.Exception", definition.getSourceFaultTypes().get(0));
        assertEquals("java.lang.String", definition.getTargetParameterTypes().get(0));
        assertEquals("java.lang.String", definition.getTargetReturnType());
        assertEquals("java.lang.Exception", definition.getTargetFaultTypes().get(0));
        assertTrue(definition.isRemotable());
        EasyMock.verify(resolver, registry);
    }

    public void testGenerateOneWayOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        LogicalOperation operation = createOperation();
        operation.getDefinition().setOneWay(true);
        List<LogicalOperation> list = Collections.singletonList(operation);

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list);

        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertTrue(definition.isOneWay());
        EasyMock.verify(resolver, registry);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalOperation createOperation() {
        MockImplementation implementation = new MockImplementation();
        ComponentDefinition<?> componentDefinition = new ComponentDefinition("component", implementation);
        componentDefinition.setContributionUri(CONTRIBUTION_URI);
        LogicalComponent component = new LogicalComponent(URI.create("component"), componentDefinition, null);
        LogicalService service = new LogicalService(URI.create("component#service"), null, component);
        component.addService(service);

        List<DataType> input = new ArrayList<>();
        JavaType type = new JavaType(String.class);
        input.add(type);
        DataType output = new JavaType(String.class);
        List<DataType> faults = new ArrayList<>();
        faults.add(new JavaType(Exception.class));
        Operation definition = new Operation("op", input, output, faults);
        return new LogicalOperation(definition, service);
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = 2052223324217668545L;

        private InjectingComponentType componentType = new InjectingComponentType();

        public ComponentType getComponentType() {
            return componentType;
        }

        public QName getType() {
            return null;
        }
    }
}