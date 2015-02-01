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
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.component.Component;

/**
 *
 */
public class ComponentManagerImplTestCase extends TestCase {
    private static final URI DOMAIN = URI.create("sca://localhost/");
    private static final URI ROOT1 = DOMAIN.resolve("root1");
    private static final URI GRANDCHILD = DOMAIN.resolve("parent/child2/grandchild");

    private ComponentManagerImpl manager;

    public void testRegister() throws Exception {
        Component root = EasyMock.createMock(Component.class);
        EasyMock.expect(root.getUri()).andReturn(ROOT1);
        EasyMock.replay(root);
        manager.register(root);
        assertEquals(root, manager.getComponent(ROOT1));
        EasyMock.verify(root);

        EasyMock.reset(root);
        EasyMock.expect(root.getUri()).andReturn(ROOT1);
        EasyMock.replay(root);
        manager.unregister(root.getUri());
        EasyMock.verify(root);
        assertEquals(null, manager.getComponent(ROOT1));
    }

    public void testRegisterGrandchild() throws Exception {
        Component root = EasyMock.createMock(Component.class);
        EasyMock.expect(root.getUri()).andReturn(GRANDCHILD);
        EasyMock.replay(root);
        manager.register(root);
        assertEquals(root, manager.getComponent(GRANDCHILD));
        EasyMock.verify(root);
    }

    public void testRegisterDuplicate() throws Exception {
        Component root = EasyMock.createMock(Component.class);
        EasyMock.expect(root.getUri()).andReturn(ROOT1);
        EasyMock.replay(root);

        Component duplicate = EasyMock.createMock(Component.class);
        EasyMock.expect(duplicate.getUri()).andReturn(ROOT1);
        EasyMock.replay(duplicate);

        manager.register(root);
        assertEquals(root, manager.getComponent(ROOT1));
        try {
            manager.register(duplicate);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
        assertEquals(root, manager.getComponent(ROOT1));
        EasyMock.verify(root);
        EasyMock.verify(duplicate);
    }

    public void testGetComponentsInHierarchy() throws Exception {
        Component c1 = EasyMock.createMock(Component.class);
        URI uri1 = URI.create("sca://fabric/component1");
        EasyMock.expect(c1.getUri()).andReturn(uri1).atLeastOnce();
        Component c2 = EasyMock.createMock(Component.class);
        URI uri2 = URI.create("sca://fabric/component2");
        EasyMock.expect(c2.getUri()).andReturn(uri2).atLeastOnce();
        Component c3 = EasyMock.createMock(Component.class);
        EasyMock.expect(c3.getUri()).andReturn(URI.create("sca://other/component3")).atLeastOnce();
        EasyMock.replay(c1, c2, c3);
        manager.register(c1);
        manager.register(c2);
        manager.register(c3);
        List<Component> components = manager.getComponentsInHierarchy(URI.create("sca://fabric"));
        assertEquals(2, components.size());
        assertTrue(components.contains(c1));
        assertTrue(components.contains(c2));
        components = manager.getComponentsInHierarchy(URI.create("sca://fabric/component1"));
        assertEquals(1, components.size());
        assertTrue(components.contains(c1));
    }

    public void testGetComponents() throws Exception {
        Component component = EasyMock.createMock(Component.class);
        EasyMock.expect(component.getUri()).andReturn(ROOT1);
        EasyMock.replay(component);
        manager.register(component);

        assertEquals(1, manager.getComponents().size());
        assertTrue(manager.getComponents().contains(component));
        EasyMock.verify(component);
    }

    public void testGetDeployedComponents() throws Exception {
        Component c1 = EasyMock.createMock(Component.class);
        URI uri1 = URI.create("sca://fabric/component1");
        QName deployable = new QName("urn:foo", "foo");
        EasyMock.expect(c1.getUri()).andReturn(uri1).atLeastOnce();
        EasyMock.expect(c1.getDeployable()).andReturn(deployable).atLeastOnce();
        Component c2 = EasyMock.createMock(Component.class);
        URI uri2 = URI.create("sca://fabric/component2");
        EasyMock.expect(c2.getUri()).andReturn(uri2).atLeastOnce();
        EasyMock.expect(c2.getDeployable()).andReturn(deployable).atLeastOnce();
        Component c3 = EasyMock.createMock(Component.class);
        QName deployable3 = new QName("urn:foo", "bar");
        EasyMock.expect(c3.getUri()).andReturn(URI.create("sca://other/component3")).atLeastOnce();
        EasyMock.expect(c3.getDeployable()).andReturn(deployable3).atLeastOnce();
        EasyMock.replay(c1, c2, c3);
        manager.register(c1);
        manager.register(c2);
        manager.register(c3);
        List<Component> components = manager.getDeployedComponents(deployable);
        assertEquals(2, components.size());
        assertTrue(components.contains(c1));
        assertTrue(components.contains(c2));

    }

    protected void setUp() throws Exception {
        super.setUp();
        manager = new ComponentManagerImpl();
    }
}
