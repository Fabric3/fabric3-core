/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.fabric.binding;

import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.binding.handler.BindingHandlerRegistryCallback;

/**
 * @version $Rev$ $Date$
 */
public class BindingHandlerRegistryImplTestCase extends TestCase {
    private static final QName BINDING = new QName("foo", "bar");

    private BindingHandlerRegistryImpl registry;
    private BindingHandler<Object> handler;
    private BindingHandlerRegistryCallback<Object> callback;

    @SuppressWarnings({"unchecked"})
    public void testRegisterUnregisterHandler() throws Exception {
        callback.update(EasyMock.isA(List.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(callback, handler);

        registry.register(callback);
        registry.register(handler);
        registry.unregister(handler);
        EasyMock.verify(callback, handler);
    }

    @SuppressWarnings({"unchecked"})
    public void testRegisterUnregisterCallback() throws Exception {
        callback.update(EasyMock.isA(List.class));
        EasyMock.expectLastCall();
        EasyMock.replay(callback, handler);

        registry.register(handler);
        registry.register(callback);
        registry.unregister(callback);
        registry.unregister(handler);
        EasyMock.verify(callback, handler);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void setUp() throws Exception {
        super.setUp();
        registry = new BindingHandlerRegistryImpl(null);
        handler = EasyMock.createMock(BindingHandler.class);
        EasyMock.expect(handler.getType()).andReturn(BINDING).anyTimes();

        callback = EasyMock.createMock(BindingHandlerRegistryCallback.class);
        EasyMock.expect(callback.getType()).andReturn(BINDING).anyTimes();

    }
}
