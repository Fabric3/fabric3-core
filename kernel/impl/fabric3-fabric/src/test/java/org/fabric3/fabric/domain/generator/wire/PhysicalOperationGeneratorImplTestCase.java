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
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.policy.PolicyMetadata;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.Constants;
import org.w3c.dom.Element;

/**
 *
 */
public class PhysicalOperationGeneratorImplTestCase extends TestCase {
    private static final QName OASIS_ONEWAY = new QName(Constants.SCA_NS, "oneWay");
    private static final QName ALLOWS_BY_REFERENCE = new QName(org.fabric3.api.Namespaces.F3, "allowsPassByReference");

    private static final URI CONTRIBUTION_URI = URI.create("contribution");
    private static final URI POLICY_URI = URI.create("policy");

    public void testGenerateNoPolicyOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        List<LogicalOperation> list = Collections.singletonList(createOperation());

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list, false, null);

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

    public void testGeneratePolicyOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        InterceptorGenerator interceptorGenerator = createInterceptorGenerator(registry);
        Element expression = createElement();
        EasyMock.replay(resolver, registry, expression, interceptorGenerator);

        LogicalOperation operation = createOperation();
        List<LogicalOperation> list = Collections.singletonList(operation);

        MockPolicyResult result = createPolicyResult(operation, expression);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list, false, result);

        assertEquals(1, definitions.size());
        PhysicalOperationDefinition definition = definitions.iterator().next();
        Set<PhysicalInterceptorDefinition> interceptors = definition.getInterceptors();
        assertFalse(interceptors.isEmpty());
        PhysicalInterceptorDefinition interceptorDefinition = interceptors.iterator().next();
        assertEquals(CONTRIBUTION_URI, interceptorDefinition.getWireClassLoaderId());
        assertEquals(POLICY_URI, interceptorDefinition.getPolicyClassLoaderId());

        EasyMock.verify(resolver, registry, expression, interceptorGenerator);
    }

    public void testGenerateRemoteOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        generator.setPassByValueEnabled(true);
        LogicalOperation operation = createOperation();
        operation.getDefinition().setRemotable(true);
        List<LogicalOperation> list = Collections.singletonList(operation);

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list, true, null);

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

    public void testGenerateAllowsByReferenceRemoteOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        generator.setPassByValueEnabled(true);
        LogicalOperation operation = createOperation();
        operation.getDefinition().setRemotable(true);
        operation.getIntents().contains(ALLOWS_BY_REFERENCE);
        List<LogicalOperation> list = Collections.singletonList(operation);

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list, true, null);

        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertTrue(definition.isRemotable());
        assertTrue(definition.isAllowsPassByReference());
        EasyMock.verify(resolver, registry);
    }

    public void testGeneratePassByValueRemoteOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        generator.setPassByValueEnabled(true);
        LogicalOperation operation = createOperation();
        operation.getDefinition().setRemotable(true);
        List<LogicalOperation> list = Collections.singletonList(operation);

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list, false, null);

        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertTrue(definition.isRemotable());
        assertFalse(definition.isAllowsPassByReference());
        EasyMock.verify(resolver, registry);
    }

    public void testGenerateOneWayOperation() throws Exception {
        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.replay(resolver, registry);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        LogicalOperation operation = createOperation();
        operation.getDefinition().addIntent(OASIS_ONEWAY);
        List<LogicalOperation> list = Collections.singletonList(operation);

        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(list, false, null);

        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertTrue(definition.isOneWay());
        EasyMock.verify(resolver, registry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGeneratePolicySourceTargetOperations() throws Exception {
        LogicalOperation source = createOperation();
        List<LogicalOperation> sources = Collections.singletonList(source);
        LogicalOperation target = createTargetOperation();
        List<LogicalOperation> targets = Collections.singletonList(target);

        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        EasyMock.expect(resolver.resolve(EasyMock.isA(LogicalOperation.class), EasyMock.isA(List.class))).andReturn(target);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        InterceptorGenerator interceptorGenerator = createInterceptorGenerator(registry);
        Element expression = createElement();
        EasyMock.replay(resolver, registry, expression, interceptorGenerator);

        MockPolicyResult result = createPolicyResult(source, expression);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(sources, targets, false, result);

        assertEquals(1, definitions.size());
        PhysicalOperationDefinition definition = definitions.iterator().next();
        Set<PhysicalInterceptorDefinition> interceptors = definition.getInterceptors();
        assertFalse(interceptors.isEmpty());
        PhysicalInterceptorDefinition interceptorDefinition = interceptors.iterator().next();
        assertEquals(CONTRIBUTION_URI, interceptorDefinition.getWireClassLoaderId());
        assertEquals(POLICY_URI, interceptorDefinition.getPolicyClassLoaderId());
        assertEquals("java.lang.String", definition.getSourceParameterTypes().get(0));
        assertEquals("java.lang.String", definition.getSourceReturnType());
        assertEquals("java.lang.Exception", definition.getSourceFaultTypes().get(0));
        assertEquals("java.lang.Object", definition.getTargetParameterTypes().get(0));
        assertEquals("java.lang.Object", definition.getTargetReturnType());
        assertEquals("java.lang.RuntimeException", definition.getTargetFaultTypes().get(0));


        EasyMock.verify(resolver, registry, expression, interceptorGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGeneratePolicySourceTargetOneWayOperations() throws Exception {
        LogicalOperation source = createOperation();
        source.getDefinition().addIntent(OASIS_ONEWAY);
        List<LogicalOperation> sources = Collections.singletonList(source);
        LogicalOperation target = createTargetOperation();
        List<LogicalOperation> targets = Collections.singletonList(target);
        target.getDefinition().addIntent(OASIS_ONEWAY);

        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        EasyMock.expect(resolver.resolve(EasyMock.isA(LogicalOperation.class), EasyMock.isA(List.class))).andReturn(target);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        InterceptorGenerator interceptorGenerator = createInterceptorGenerator(registry);
        Element expression = createElement();
        EasyMock.replay(resolver, registry, expression, interceptorGenerator);

        MockPolicyResult result = createPolicyResult(source, expression);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(sources, targets, false, result);

        assertEquals(1, definitions.size());
        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertTrue(definition.isOneWay());

        EasyMock.verify(resolver, registry, expression, interceptorGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testGeneratePolicySourceTargetPassByValueOperations() throws Exception {
        LogicalOperation source = createOperation();
        source.getDefinition().setRemotable(true);
        List<LogicalOperation> sources = Collections.singletonList(source);
        LogicalOperation target = createTargetOperation();
        List<LogicalOperation> targets = Collections.singletonList(target);
        target.getDefinition().setRemotable(true);

        OperationResolver resolver = EasyMock.createMock(OperationResolver.class);
        EasyMock.expect(resolver.resolve(EasyMock.isA(LogicalOperation.class), EasyMock.isA(List.class))).andReturn(target);
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        InterceptorGenerator interceptorGenerator = createInterceptorGenerator(registry);
        Element expression = createElement();
        EasyMock.replay(resolver, registry, expression, interceptorGenerator);

        MockPolicyResult result = createPolicyResult(source, expression);

        PhysicalOperationGeneratorImpl generator = new PhysicalOperationGeneratorImpl(resolver, registry);
        generator.setPassByValueEnabled(true);
        Set<PhysicalOperationDefinition> definitions = generator.generateOperations(sources, targets, false, result);

        assertEquals(1, definitions.size());
        PhysicalOperationDefinition definition = definitions.iterator().next();
        assertFalse(definition.isAllowsPassByReference());

        EasyMock.verify(resolver, registry, expression, interceptorGenerator);
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

    @SuppressWarnings({"unchecked"})
    private LogicalOperation createTargetOperation() {
        MockImplementation implementation = new MockImplementation();
        ComponentDefinition<?> componentDefinition = new ComponentDefinition("component", implementation);
        componentDefinition.setContributionUri(CONTRIBUTION_URI);
        LogicalComponent component = new LogicalComponent(URI.create("component"), componentDefinition, null);
        LogicalService service = new LogicalService(URI.create("component#service"), null, component);
        component.addService(service);

        List<DataType> input = new ArrayList<>();
        JavaType type = new JavaType(Object.class);
        input.add(type);
        DataType output = new JavaType(Object.class);
        List<DataType> faults = new ArrayList<>();
        faults.add(new JavaType(RuntimeException.class));
        Operation definition = new Operation("op", input, output, faults);
        return new LogicalOperation(definition, service);
    }

    private MockPolicyResult createPolicyResult(LogicalOperation operation, Element expression) {
        MockPolicyResult result = new MockPolicyResult();
        QName name = new QName("policy", "test");
        PolicySet policySet = new PolicySet(name, null, null, null, expression, null, null, POLICY_URI);
        result.addPolicy(operation, Collections.singletonList(policySet));
        return result;
    }

    private InterceptorGenerator createInterceptorGenerator(GeneratorRegistry registry) throws GenerationException {
        InterceptorGenerator interceptorGenerator = EasyMock.createMock(InterceptorGenerator.class);
        EasyMock.expect(registry.getInterceptorGenerator(EasyMock.isA(QName.class))).andReturn(interceptorGenerator);
        EasyMock.expect(interceptorGenerator.generate(EasyMock.isA(Element.class),
                                                      EasyMock.isA(PolicyMetadata.class),
                                                      EasyMock.isA(LogicalOperation.class))).andReturn(new MockPhysicalInterceptorDefinition());
        return interceptorGenerator;
    }

    private Element createElement() {
        Element expression = EasyMock.createMock(Element.class);
        EasyMock.expect(expression.getNamespaceURI()).andReturn("test");
        EasyMock.expect(expression.getLocalName()).andReturn("test");
        return expression;
    }


    private class MockPhysicalInterceptorDefinition extends PhysicalInterceptorDefinition {
        private static final long serialVersionUID = 7343328866305106112L;
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