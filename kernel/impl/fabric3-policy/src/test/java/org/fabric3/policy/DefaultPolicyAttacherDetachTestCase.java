/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.policy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
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
        List<Operation> operations = new ArrayList<Operation>();
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