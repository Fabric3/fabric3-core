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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.DefaultHostInfoBuilder;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.fabric.domain.collector.Collector;
import org.fabric3.fabric.domain.collector.CollectorImpl;
import org.fabric3.fabric.domain.generator.Deployment;
import org.fabric3.fabric.domain.generator.Generator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class RuntimeDomainTestCase extends TestCase {
    private static final URI COMPONENT_URI = URI.create("fabric3://domain/component");
    private static final URI CONTRIBUTION_URI = URI.create("contribution");

    private IMocksControl control;
    private AbstractDomain domain;
    private LogicalModelInstantiator instantiator;
    private Generator generator;
    private Deployer deployer;
    private LogicalComponentManagerImpl lcm;

    private Component component;
    private Composite composite;

    @SuppressWarnings({"unchecked"})
    public void testIncludeUris() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(component);
        EasyMock.expect(instantiator.include((List<Composite>) EasyMock.notNull(), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        Deployment deployment = new Deployment();
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class))).andReturn(deployment);
        deployer.deploy(EasyMock.isA(Deployment.class));

        control.replay();

        domain.include(Collections.singletonList(CONTRIBUTION_URI));

        // verify the component contained in the composite was added to the logical model
        assertNotNull(lcm.getDomainComposite().getComponent(COMPONENT_URI));
        control.verify();
    }

    public void testIncludeAndRemove() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(component);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        Deployment deployment = new Deployment();
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class))).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(Deployment.class));
        EasyMock.expectLastCall().times(2);
        control.replay();

        domain.include(composite);

        assertNotNull(lcm.getDomainComposite().getComponent(COMPONENT_URI));

        domain.undeploy(composite.getContributionUri());
        // verify the component contained in the composite was added to the logical model
        assertNull(lcm.getDomainComposite().getComponent(COMPONENT_URI));
        control.verify();
    }

    public void testUndeployComposite() throws Exception {
        IAnswer<InstantiationContext> answer = DomainTestCaseHelper.createAnswer(component);
        EasyMock.expect(instantiator.include(EasyMock.eq(composite), EasyMock.isA(LogicalCompositeComponent.class))).andStubAnswer(answer);

        Deployment deployment = new Deployment();
        EasyMock.expect(generator.generate(EasyMock.isA(LogicalCompositeComponent.class))).andReturn(deployment).times(2);
        deployer.deploy(EasyMock.isA(Deployment.class));
        EasyMock.expectLastCall().times(2);
        control.replay();

        domain.include(composite);

        assertNotNull(lcm.getDomainComposite().getComponent(COMPONENT_URI));

        domain.undeploy(composite);
        // verify the component contained in the composite was added to the logical model
        assertNull(lcm.getDomainComposite().getComponent(COMPONENT_URI));
        control.verify();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        control = EasyMock.createControl();
        MetaDataStore store = control.createMock(MetaDataStore.class);

        URI uri = URI.create("fabric3://domain");

        DefaultHostInfoBuilder builder = new DefaultHostInfoBuilder();
        builder.runtimeName("runtime");
        builder.zoneName(Names.DEFAULT_ZONE);
        builder.runtimeMode(RuntimeMode.VM);
        builder.domain(uri);
        HostInfo info = builder.build();

        ContributionHelperImpl helper = new ContributionHelperImpl(store, info);

        lcm = new LogicalComponentManagerImpl(info);
        lcm.init();

        generator = control.createMock(Generator.class);
        instantiator = control.createMock(LogicalModelInstantiator.class);
        deployer = control.createMock(Deployer.class);
        Collector collector = new CollectorImpl();
        domain = new RuntimeDomain(store, generator, instantiator, lcm, deployer, collector, helper, info);

        Contribution contribution = DomainTestCaseHelper.createContribution(store);
        component = new Component("component");
        component.setContributionUri(CONTRIBUTION_URI);

        composite = DomainTestCaseHelper.createComposite(contribution, component, store);

    }

}
