/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.cm;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.component.Component;

/**
 * @version $Rev$ $Date$
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
        } catch (DuplicateComponentException e) {
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
        EasyMock.replay(c1,c2,c3);
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
        EasyMock.replay(c1,c2,c3);
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
