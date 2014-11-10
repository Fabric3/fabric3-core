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
package org.fabric3.policy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class DefaultPolicyAttacherDetachTestCase extends TestCase {
    private static final QName POLICY_SET = new QName("urn:test", "testPolicy");
    private LogicalComponent child1;
    private DefaultPolicyAttacher attacher;
    private LogicalService child1Service;
    private LogicalReference child1Reference;
    private LogicalBinding child1ReferenceBinding;

    public void testDetachComponent() throws Exception {
        attacher.detach(POLICY_SET, child1);
        // the component should not be reprovisioned, just the bindings
        assertEquals(LogicalState.NEW, child1ReferenceBinding.getState());
        assertFalse(child1.getPolicySets().contains(POLICY_SET));
    }

    public void testDetachFromService() throws Exception {
        attacher.detach(POLICY_SET, child1Service);
        for (LogicalBinding<?> binding : child1Service.getBindings()) {
            assertEquals(LogicalState.NEW, binding.getState());
        }
        assertFalse(child1Service.getPolicySets().contains(POLICY_SET));
    }

    public void testDetachFromReference() throws Exception {
        attacher.detach(POLICY_SET, child1Reference);
        for (LogicalBinding<?> binding : child1Reference.getBindings()) {
            assertEquals(LogicalState.NEW, binding.getState());
        }
        assertFalse(child1Reference.getPolicySets().contains(POLICY_SET));
    }

    public void testDetachFromBinding() throws Exception {
        attacher.detach(POLICY_SET, child1ReferenceBinding);
        assertEquals(LogicalState.NEW, child1ReferenceBinding.getState());
        assertFalse(child1ReferenceBinding.getPolicySets().contains(POLICY_SET));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createDomain();
        attacher = new DefaultPolicyAttacher(null, null);
    }

    @SuppressWarnings({"unchecked"})
    private void createDomain() {
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        ComponentDefinition definition1 = new ComponentDefinition("child1");
        definition1.setImplementation(new MockImplementation());
        child1 = new LogicalComponent(child1Uri, definition1, domain);
        child1.setState(LogicalState.PROVISIONED);
        child1.addPolicySet(POLICY_SET);
        ServiceContract referenceContract = new JavaServiceContract();
        referenceContract.setInterfaceName("ChildService");
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("child1Reference", referenceContract);
        child1Reference = new LogicalReference(URI.create("child1#child1Reference"), referenceDefinition, child1);
        BindingDefinition definiton = new MockBindingDefintion();
        child1ReferenceBinding = new LogicalBinding(definiton, child1Reference);
        child1ReferenceBinding.addPolicySet(POLICY_SET);
        child1ReferenceBinding.setState(LogicalState.PROVISIONED);
        child1Reference.addBinding(child1ReferenceBinding);
        child1Reference.addPolicySet(POLICY_SET);
        child1.addReference(child1Reference);
        ServiceContract serviceContract = new JavaServiceContract();
        serviceContract.setInterfaceName("ChildService");
        Operation operation = new Operation("operation", null, null, null);
        List<Operation> operations = new ArrayList<>();
        operations.add(operation);
        serviceContract.setOperations(operations);
        ServiceDefinition serviceDefinition = new ServiceDefinition("child1Service", serviceContract);
        child1Service = new LogicalService(URI.create("child1#child1Service"), serviceDefinition, child1);
        child1Service.addBinding(child1ReferenceBinding);
        child1Service.addPolicySet(POLICY_SET);
        child1.addService(child1Service);
        domain.addComponent(child1);
    }

    private class MockBindingDefintion extends BindingDefinition {
        private static final long serialVersionUID = -5325959511447059266L;

        public MockBindingDefintion() {
            super(null, new QName(null, "binding.mock"));
        }
    }

    private class MockImplementation extends Implementation {

        private static final long serialVersionUID = 864374876504388888L;

        public QName getType() {
            return new QName(null, "implementation.mock");
        }
    }


}