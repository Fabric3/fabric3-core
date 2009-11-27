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

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Unit tests for the composite scope container
 *
 * @version $Rev$ $Date$
 */
public class StatelessScopeContainerTestCase<T> extends TestCase {
    private StatelessScopeContainer scopeContainer;
    private IMocksControl control;
    private AtomicComponent<T> component;
    private InstanceWrapper<T> wrapper;
    private WorkContext workContext;

    public void testCorrectScope() {
        assertEquals(Scope.STATELESS, scopeContainer.getScope());
    }

    public void testInstanceCreation() throws Exception {
        @SuppressWarnings("unchecked")
        InstanceWrapper<T> wrapper2 = control.createMock(InstanceWrapper.class);

        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        wrapper.start(workContext);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper2);
        wrapper2.start(workContext);
        control.replay();

        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        assertSame(wrapper2, scopeContainer.getWrapper(component, workContext));
        control.verify();
    }

    public void testReturnWrapper() throws Exception {
        wrapper.stop(workContext);
        control.replay();
        scopeContainer.returnWrapper(component, workContext, wrapper);
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        scopeContainer = new StatelessScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));

        control = EasyMock.createStrictControl();
        workContext = control.createMock(WorkContext.class);
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
    }
}
