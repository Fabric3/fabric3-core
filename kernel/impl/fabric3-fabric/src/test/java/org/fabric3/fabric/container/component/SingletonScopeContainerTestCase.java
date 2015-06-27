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
package org.fabric3.fabric.container.component;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopedComponent;

/**
 *
 */
public class SingletonScopeContainerTestCase extends TestCase {
    private ScopedComponent component;
    private URI contibutionUri;
    private SingletonScopeContainer container;
    private Object instance;

    public void testRegisterUnregister() throws Exception {
        EasyMock.replay(component);
        container.register(component);
        container.unregister(component);

        // verify the component is removed and not started
        container.startContext(contibutionUri);
        container.stopContext(contibutionUri);

        EasyMock.verify(component);
    }

    public void testUpdated() throws Exception {
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component);

        container.register(component);
        container.startContext(contibutionUri);
        container.stopContext(contibutionUri);

        EasyMock.verify(component);
    }

    public void testRemoved() throws Exception {
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component);

        container.register(component);
        container.startContext(contibutionUri);
        container.stopContext(contibutionUri);

        EasyMock.verify(component);
    }

    public void testReinject() throws Exception {
        EasyMock.expect(component.createInstance()).andReturn(instance);

        component.startInstance(EasyMock.isA(Object.class));
        component.reinject(instance);
        component.stopInstance(EasyMock.isA(Object.class));

        EasyMock.replay(component);

        container.register(component);
        container.startContext(contibutionUri);
        container.reinject();
        container.stopContext(contibutionUri);

        EasyMock.verify(component);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ScopeContainerMonitor monitor = EasyMock.createNiceMock(ScopeContainerMonitor.class);
        EasyMock.replay(monitor);
        container = new SingletonScopeContainer(Scope.COMPOSITE, monitor) {
        };

        contibutionUri = URI.create("deployable");

        component = EasyMock.createMock(ScopedComponent.class);
        EasyMock.expect(component.getContributionUri()).andReturn(contibutionUri).atLeastOnce();
        EasyMock.expect(component.isEagerInit()).andReturn(true).atLeastOnce();

        instance = new Object();

    }

}
