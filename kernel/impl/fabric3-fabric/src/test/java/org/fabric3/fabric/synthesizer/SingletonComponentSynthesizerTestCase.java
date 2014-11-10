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
package org.fabric3.fabric.synthesizer;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Names;
import org.fabric3.fabric.container.component.ComponentManagerImpl;
import org.fabric3.fabric.container.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.container.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.domain.LogicalComponentManagerImpl;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class SingletonComponentSynthesizerTestCase extends TestCase {

    private ImplementationIntrospector implementationIntrospector;
    private AtomicComponentInstantiator instantiator;
    private LogicalComponentManagerImpl lcm;
    private ComponentManager componentManager;
    private JavaContractProcessor contractProcessor;
    private ScopeContainer scopeContainer;

    public void testSynthesizeIntrospect() throws Exception {
        SingletonComponentSynthesizer synthesizer = new SingletonComponentSynthesizer(implementationIntrospector,
                                                                                      instantiator,
                                                                                      lcm,
                                                                                      componentManager,
                                                                                      contractProcessor,
                                                                                      scopeContainer);

        synthesizer.registerComponent("component", TestService.class, new TestComponent(), true);

        URI uri = URI.create(Names.RUNTIME_NAME + "/component");
        assertNotNull(lcm.getComponent(uri));
        AtomicComponent component = (AtomicComponent) componentManager.getComponent(uri);
        assertNotNull(component);
        assertNotNull(component.getInstance());
    }

    public void testSynthesizeNoIntrospect() throws Exception {
        SingletonComponentSynthesizer synthesizer = new SingletonComponentSynthesizer(implementationIntrospector,
                                                                                      instantiator,
                                                                                      lcm,
                                                                                      componentManager,
                                                                                      contractProcessor,
                                                                                      scopeContainer);

        synthesizer.registerComponent("component", TestService.class, new TestComponent(), false);

        URI uri = URI.create(Names.RUNTIME_NAME + "/component");
        assertNotNull(lcm.getComponent(uri));
        AtomicComponent component = (AtomicComponent) componentManager.getComponent(uri);
        assertNotNull(component);
        assertNotNull(component.getInstance());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        implementationIntrospector = EasyMock.createNiceMock(ImplementationIntrospector.class);
        instantiator = new AtomicComponentInstantiatorImpl();
        lcm = new LogicalComponentManagerImpl();
        lcm.init();
        componentManager = new ComponentManagerImpl();
        ScopeContainerMonitor monitor = EasyMock.createNiceMock(ScopeContainerMonitor.class);
        EasyMock.replay(monitor);
        scopeContainer = new CompositeScopeContainer(monitor);
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        contractProcessor = new JavaContractProcessorImpl(helper);

    }

    private class TestComponent implements TestService {
        @Reference
        protected Object reference;

    }

    private interface TestService {

    }
}
