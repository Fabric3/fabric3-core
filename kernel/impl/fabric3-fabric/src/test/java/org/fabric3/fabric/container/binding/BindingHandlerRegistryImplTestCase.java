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
 */
package org.fabric3.fabric.container.binding;

import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistryCallback;

/**
 *
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
