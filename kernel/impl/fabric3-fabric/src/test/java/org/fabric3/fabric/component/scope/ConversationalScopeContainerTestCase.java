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

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public class ConversationalScopeContainerTestCase extends TestCase {
    private ConversationalScopeContainer container;
    private IMocksControl control;
    private InstanceWrapperStore<F3Conversation> store;
    private F3Conversation conversation;
    private WorkContext workContext;
    private AtomicComponent component;
    private InstanceWrapper wrapper;

    public void testStoreIsNotifiedOfContextStartStop() throws Exception {
        store.startContext(conversation);
        store.stopContext(conversation);
        control.replay();
        container.startContext(workContext);
        container.stopContext(workContext);
        control.verify();
    }

    public void testWrapperCreatedIfNotFound() throws Exception {

        EasyMock.expect(store.getWrapper(component, conversation)).andReturn(null);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        wrapper.start(EasyMock.isA(WorkContext.class));
        store.putWrapper(component, conversation, wrapper);
        store.startContext(EasyMock.eq(conversation));
        control.replay();
        container.startContext(workContext);
        assertSame(wrapper, container.getWrapper(component, workContext));
        control.verify();
    }

    public void testWrapperReturnedIfFound() throws Exception {
        EasyMock.expect(store.getWrapper(component, conversation)).andReturn(wrapper);
        control.replay();
        assertSame(wrapper, container.getWrapper(component, workContext));
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        control = EasyMock.createControl();
        store = control.createMock(InstanceWrapperStore.class);
        container = new ConversationalScopeContainer(null, store);
        conversation = EasyMock.createMock(F3Conversation.class);
        workContext = new WorkContext();
        CallFrame frame = new CallFrame(null, null, conversation, ConversationContext.NEW);
        workContext.addCallFrame(frame);
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
    }

}
