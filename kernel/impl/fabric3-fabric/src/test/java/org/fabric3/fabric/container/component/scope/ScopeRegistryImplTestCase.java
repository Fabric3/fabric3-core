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
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopeContainer;

/**
 *
 */
public class ScopeRegistryImplTestCase extends TestCase {
    private ScopeContainer container;
    private ScopeRegistryImpl registry;

    public void testGetScopeEnum() throws Exception {
        registry.register(container);
        assertEquals(container, registry.getScopeContainer(Scope.COMPOSITE));
        registry.unregister(container);
        assertNull(registry.getScopeContainer(Scope.COMPOSITE));
        EasyMock.verify(container);
    }

    protected void setUp() throws Exception {
        super.setUp();
        container = EasyMock.createMock(ScopeContainer.class);
        EasyMock.expect(container.getScope()).andReturn(Scope.COMPOSITE).anyTimes();
        EasyMock.replay(container);

        registry = new ScopeRegistryImpl();
    }
}
