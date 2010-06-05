/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.fabric.component.scope;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $$Rev$$ $$Date$$
 */
public class CompositeScopeContainerTestCase extends TestCase {
    protected IMocksControl control;
    protected CompositeScopeContainer scopeContainer;
    protected QName deployable;
    protected AtomicComponent component;
    protected InstanceWrapper wrapper;
    private WorkContext workContext;

    public void testCorrectScope() {
        assertEquals(Scope.COMPOSITE, scopeContainer.getScope());
    }

    public void testWrapperCreation() throws Exception {

        EasyMock.expect(component.isEagerInit()).andStubReturn(false);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        EasyMock.expect(component.getDeployable()).andStubReturn(deployable);
        control.replay();
        scopeContainer.register(component);
        scopeContainer.startContext(workContext);
        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        control.verify();
    }

    protected void setUp() throws Exception {
        super.setUp();
        deployable = new QName("deployable");
        control = EasyMock.createStrictControl();
        workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame(deployable));
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
        scopeContainer = new CompositeScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));
        scopeContainer.start();
    }
}
