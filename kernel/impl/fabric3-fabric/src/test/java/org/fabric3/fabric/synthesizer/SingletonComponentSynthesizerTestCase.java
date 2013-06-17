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
*/
package org.fabric3.fabric.synthesizer;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.fabric3.fabric.cm.ComponentManagerImpl;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.lcm.LogicalComponentManagerImpl;
import org.fabric3.fabric.runtime.bootstrap.BootstrapIntrospectionFactory;
import org.fabric3.host.Names;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class SingletonComponentSynthesizerTestCase extends TestCase {

    private ImplementationProcessor implementationProcessor;
    private AtomicComponentInstantiator instantiator;
    private LogicalComponentManagerImpl lcm;
    private ComponentManager componentManager;
    private JavaContractProcessor contractProcessor;
    private ScopeContainer scopeContainer;

    public void testSynthesizeIntrospect() throws Exception {
        SingletonComponentSynthesizer synthesizer = new SingletonComponentSynthesizer(implementationProcessor,
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
        SingletonComponentSynthesizer synthesizer = new SingletonComponentSynthesizer(implementationProcessor,
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
        implementationProcessor = BootstrapIntrospectionFactory.createSystemImplementationProcessor();
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
