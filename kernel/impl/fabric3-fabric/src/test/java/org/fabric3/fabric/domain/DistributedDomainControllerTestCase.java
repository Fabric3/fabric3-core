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
package org.fabric3.fabric.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.fabric3.fabric.generator.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.collector.CollectorImpl;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.DomainJournal;
import org.fabric3.host.runtime.DefaultHostInfo;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.Allocator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.generator.policy.PolicyAttacher;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Verifies the distributed domain when run in Controller, transactional mode.
 */
public class DistributedDomainControllerTestCase extends TestCase {
    private static final URI COMPONENT_URI = URI.create("fabric3://domain/component");
    private static final URI CONTRIBUTION_URI = URI.create("contribution");

    private static final QName DEPLOYABLE = new QName("foo", "bar");

    private IMocksControl control;
    private DistributedDomain domain;
    private LogicalModelInstantiator instantiator;
    private PolicyAttacher policyAttacher;
    private BindingSelector bindingSelector;
    private Generator generator;
    private Deployer deployer;
    private LogicalComponentManagerImpl lcm;

    private Contribution contribution;
    private ComponentDefinition componentDefinition;
    private Composite composite;

    public void testInclude() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        policyAttacher.attachPolicies(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());
        bindingSelector.selectBindings(EasyMock.isA(LogicalCompositeComponent.class));

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));

        control.replay();

        domain.include(DEPLOYABLE);

        // verify the component contained in the composite was added to the logical model
        assertNotNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        assertTrue(contribution.getLockOwners().contains(DEPLOYABLE));
        control.verify();
    }

    @SuppressWarnings({"unchecked"})
    public void testIncludeUris() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include((List<Composite>) EasyMock.notNull(), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        policyAttacher.attachPolicies(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());
        bindingSelector.selectBindings(EasyMock.isA(LogicalCompositeComponent.class));

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));

        control.replay();

        domain.include(Collections.<URI>singletonList(CONTRIBUTION_URI));

        // verify the component contained in the composite was added to the logical model
        assertNotNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        control.verify();
    }

    public void testIncludeAndRemove() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        policyAttacher.attachPolicies(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());
        bindingSelector.selectBindings(EasyMock.isA(LogicalCompositeComponent.class));

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(4);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));
        EasyMock.expectLastCall().times(2);
        control.replay();

        domain.include(DEPLOYABLE);

        assertNotNull(lcm.getRootComponent().getComponent(COMPONENT_URI));

        domain.undeploy(composite.getContributionUri(), false);
        // verify the component contained in the composite was added to the logical model
        assertNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        control.verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testActivateDeactivateDefinitions() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(4);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));
        EasyMock.expectLastCall().times(2);

        PolicyRegistry policyRegistry = control.createMock(PolicyRegistry.class);
        Set<PolicySet> set = new HashSet<PolicySet>();
        set.add(new PolicySet(new QName("foo", "bar"), null, null, null, null, null, null, null));
        EasyMock.expect(policyRegistry.activateDefinitions(CONTRIBUTION_URI)).andReturn(set);
        EasyMock.expect(policyRegistry.deactivateDefinitions(CONTRIBUTION_URI)).andReturn(set);
        domain.setPolicyRegistry(policyRegistry);

        policyAttacher.attachPolicies(EasyMock.isA(Set.class), EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());
        policyAttacher.detachPolicies(EasyMock.isA(Set.class), EasyMock.isA(LogicalCompositeComponent.class));

        control.replay();

        domain.activateDefinitions(CONTRIBUTION_URI);

        domain.deactivateDefinitions(CONTRIBUTION_URI);
        control.verify();
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testActivateDefinitionsError() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));
        // simulate a deployment exception
        EasyMock.expectLastCall().andThrow(new DeploymentException());

        PolicyRegistry policyRegistry = control.createMock(PolicyRegistry.class);
        Set<PolicySet> set = new HashSet<PolicySet>();
        set.add(new PolicySet(new QName("foo", "bar"), null, null, null, null, null, null, null));
        EasyMock.expect(policyRegistry.activateDefinitions(CONTRIBUTION_URI)).andReturn(set);
        domain.setPolicyRegistry(policyRegistry);

        policyAttacher.attachPolicies(EasyMock.isA(Set.class), EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());

        control.replay();

        try {
            domain.activateDefinitions(CONTRIBUTION_URI);
            fail();
        } catch (DeploymentException e) {
            // expected
        }
        // verify the component contained in the composite was *not* added to the logical model as an error was raised during deployment
        assertNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        control.verify();
    }

    @SuppressWarnings({"unchecked"})
    public void testRecover() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include((List<Composite>) EasyMock.notNull(), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        policyAttacher.attachPolicies(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());
        bindingSelector.selectBindings(EasyMock.isA(LogicalCompositeComponent.class));

        // generation and deployment should not be done
        control.replay();

        Map<QName, String> deployables = Collections.singletonMap(DEPLOYABLE, "fabric3.synthetic");
        DomainJournal journal = new DomainJournal(Collections.<URI>emptyList(), deployables);
        domain.recover(journal);

        // verify the component contained in the composite was added to the logical model
        assertNotNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        control.verify();
    }

    public void testAllocate() throws Exception {
        Allocator allocator = EasyMock.createMock(Allocator.class);
        allocator.allocate(EasyMock.isA(LogicalComponent.class), EasyMock.isA(DeploymentPlan.class));

        domain.setAllocator(allocator);

        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        policyAttacher.attachPolicies(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());
        bindingSelector.selectBindings(EasyMock.isA(LogicalCompositeComponent.class));

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));

        control.replay();

        domain.include(DEPLOYABLE);

        // verify the component contained in the composite was added to the logical model
        assertNotNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        assertTrue(contribution.getLockOwners().contains(DEPLOYABLE));
        control.verify();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URI uri = URI.create("fabric3://domain");
        // set controller mode
        HostInfo info = new DefaultHostInfo("runtime",
                                            Names.DEFAULT_ZONE,
                                            RuntimeMode.CONTROLLER,
                                            null,
                                            uri,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            false);

        control = EasyMock.createControl();
        MetaDataStore store = control.createMock(MetaDataStore.class);

        ContributionHelperImpl helper = new ContributionHelperImpl(store, info);

        lcm = new LogicalComponentManagerImpl(info);
        lcm.init();

        generator = control.createMock(Generator.class);
        instantiator = control.createMock(LogicalModelInstantiator.class);

        policyAttacher = control.createMock(PolicyAttacher.class);
        bindingSelector = control.createMock(BindingSelector.class);
        deployer = control.createMock(Deployer.class);
        Collector collector = new CollectorImpl();
        domain = new DistributedDomain(store, lcm, generator, instantiator, policyAttacher, bindingSelector, deployer, collector, helper, info);
        domain.setTransactional(true);     // set transactional mode

        contribution = DomainTestCaseHelper.createContribution(store);
        componentDefinition = new ComponentDefinition("component");
        composite = DomainTestCaseHelper.createComposite(contribution, componentDefinition, store);
    }

}
