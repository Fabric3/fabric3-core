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
package org.fabric3.fabric.container.component.scope;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopedComponent;

/**
 * Unit tests for the composite scope container
 */
public class StatelessScopeContainerTestCase extends TestCase {
    private StatelessScopeContainer scopeContainer;
    private IMocksControl control;
    private ScopedComponent component;
    private Object instance;

    public void testCorrectScope() {
        assertEquals(Scope.STATELESS, scopeContainer.getScope());
    }

    public void testInstanceCreation() throws Exception {
        instance = new Object();

        EasyMock.expect(component.createInstance()).andReturn(this.instance);
        component.startInstance(this.instance);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        component.startInstance(instance);
        control.replay();

        assertSame(this.instance, scopeContainer.getInstance(component));
        assertSame(instance, scopeContainer.getInstance(component));
        control.verify();
    }

    public void testReturnWrapper() throws Exception {
        component.stopInstance(instance);
        control.replay();
        scopeContainer.releaseInstance(component, instance);
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        scopeContainer = new StatelessScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));

        control = EasyMock.createStrictControl();
        component = control.createMock(ScopedComponent.class);
        instance = new Object();
    }
}
