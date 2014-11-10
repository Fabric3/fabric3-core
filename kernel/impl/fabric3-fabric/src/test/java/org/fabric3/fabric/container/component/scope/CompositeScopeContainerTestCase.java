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

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopedComponent;

/**
 *
 */
public class CompositeScopeContainerTestCase extends TestCase {
    protected IMocksControl control;
    protected CompositeScopeContainer scopeContainer;
    protected QName deployable;
    protected ScopedComponent component;
    protected Object instance;

    public void testCorrectScope() {
        assertEquals(Scope.COMPOSITE, scopeContainer.getScope());
    }

    public void testInstanceCreation() throws Exception {

        EasyMock.expect(component.isEagerInit()).andStubReturn(false);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        component.startInstance(EasyMock.isA(Object.class));
        EasyMock.expect(component.getDeployable()).andStubReturn(deployable);
        control.replay();
        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        assertSame(instance, scopeContainer.getInstance(component));
        assertSame(instance, scopeContainer.getInstance(component));
        control.verify();
    }

    public void testEagerInit() throws Exception {

        EasyMock.expect(component.isEagerInit()).andStubReturn(true);
        EasyMock.expect(component.createInstance()).andReturn(instance);
        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));
        EasyMock.expect(component.getDeployable()).andStubReturn(deployable);
        control.replay();
        scopeContainer.register(component);
        scopeContainer.startContext(deployable);
        scopeContainer.stopContext(deployable);
        control.verify();
    }

    protected void setUp() throws Exception {
        super.setUp();
        deployable = new QName("deployable");
        control = EasyMock.createStrictControl();
        component = control.createMock(ScopedComponent.class);
        instance = new Object();
        scopeContainer = new CompositeScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));
        scopeContainer.start();
    }
}
