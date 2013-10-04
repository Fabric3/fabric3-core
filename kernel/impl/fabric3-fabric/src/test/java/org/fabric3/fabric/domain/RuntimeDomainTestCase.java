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
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.fabric.deployment.instantiator.LogicalModelInstantiator;
import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.DefaultHostInfo;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.Deployer;
import org.fabric3.spi.domain.DeploymentPackage;
import org.fabric3.spi.deployment.generator.Deployment;
import org.fabric3.spi.deployment.generator.Generator;
import org.fabric3.spi.deployment.generator.policy.PolicyAttacher;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class RuntimeDomainTestCase extends TestCase {
    private static final URI COMPONENT_URI = URI.create("fabric3://domain/component");
    private static final URI CONTRIBUTION_URI = URI.create("contribution");

    private static final QName DEPLOYABLE = new QName("foo", "bar");

    private IMocksControl control;
    private AbstractDomain domain;
    private LogicalModelInstantiator instantiator;
    private PolicyAttacher policyAttacher;
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

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment);
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

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment);
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

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(2);
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

    public void testUndeployComposite() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(componentDefinition);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        policyAttacher.attachPolicies(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean());

        Deployment deployment = new Deployment("1");
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class), EasyMock.anyBoolean())).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(DeploymentPackage.class));
        EasyMock.expectLastCall().times(2);
        control.replay();

        domain.include(DEPLOYABLE);

        assertNotNull(lcm.getRootComponent().getComponent(COMPONENT_URI));

        domain.undeploy(composite, false);
        // verify the component contained in the composite was added to the logical model
        assertNull(lcm.getRootComponent().getComponent(COMPONENT_URI));
        control.verify();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        control = EasyMock.createControl();
        MetaDataStore store = control.createMock(MetaDataStore.class);

        URI uri = URI.create("fabric3://domain");
        HostInfo info = new DefaultHostInfo("runtime",
                                            Names.DEFAULT_ZONE,
                                            RuntimeMode.VM,
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
        ContributionHelperImpl helper = new ContributionHelperImpl(store, info);

        lcm = new LogicalComponentManagerImpl(info);
        lcm.init();

        generator = control.createMock(Generator.class);
        instantiator = control.createMock(LogicalModelInstantiator.class);
        policyAttacher = control.createMock(PolicyAttacher.class);
        deployer = control.createMock(Deployer.class);
        Collector collector = new CollectorImpl();
        domain = new RuntimeDomain(store, generator, instantiator, policyAttacher, lcm, deployer, collector, helper, info);

        contribution = DomainTestCaseHelper.createContribution(store);
        componentDefinition = new ComponentDefinition("component");
        composite = DomainTestCaseHelper.createComposite(contribution, componentDefinition, store);

    }

}
