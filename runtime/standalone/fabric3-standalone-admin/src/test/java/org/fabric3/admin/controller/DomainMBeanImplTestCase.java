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
*/
package org.fabric3.admin.controller;

import java.net.URI;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.InvalidPathException;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev$ $Date$
 */
public class DomainMBeanImplTestCase extends TestCase {
    private DistributedDomainMBean mBean;
    private static final URI DOMAIN = URI.create("fabric3://domain");
    private static final URI CHILD1 = URI.create("fabric3://domain/child1");
    private static final URI CHILD2 = URI.create("fabric3://domain/child2");
    private static final URI GRAND_CHILD1 = URI.create("fabric3://domain/child1/grandChild1");
    private static final URI GRAND_GRAND_CHILD = URI.create("fabric3://domain/child1/grandChild1/grandGrandChild");

    public void testListPath() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/child1");
        assertEquals(1, infos.size());
        assertEquals(GRAND_CHILD1, infos.get(0).getUri());
    }

    public void testListDomain() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/");
        assertEquals(2, infos.size());
        URI uri = infos.get(0).getUri();
        assertTrue(CHILD1 == uri || CHILD2 == uri);
        uri = infos.get(1).getUri();
        assertTrue(CHILD1 == uri || CHILD2 == uri);
    }

    public void testListGrandGrandChild() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/child1/grandChild1");
        assertEquals(1, infos.size());
    }

    public void testListEmptyPath() throws Exception {
        List<ComponentInfo> infos = mBean.getDeployedComponents("/child2");
        assertEquals(0, infos.size());
    }

    public void testListNonExistentEmptyPath() throws Exception {
        try {
            mBean.getDeployedComponents("/child3");
            fail();
        } catch (InvalidPathException e) {
            // expected
        }
    }

    public void testInvalidPath() throws Exception {
        try {
            mBean.getDeployedComponents("invalid");
            fail();
        } catch (InvalidPathException e) {
            // expected
        }
    }

    public void testListNonCompositePath() throws Exception {
        try {
            mBean.getDeployedComponents("/child1/grandChild1/grandGrandChild");
            fail();
        } catch (InvalidPathException e) {
            // expected
        }
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(DOMAIN);
        EasyMock.replay(info);

        LogicalCompositeComponent domain = new LogicalCompositeComponent(DOMAIN, null, null);
        ComponentDefinition<CompositeImplementation> child1Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalCompositeComponent child1 = new LogicalCompositeComponent(CHILD1, child1Def, domain);
        domain.addComponent(child1);
        ComponentDefinition<CompositeImplementation> grandChild1Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalCompositeComponent grandChild1 = new LogicalCompositeComponent(GRAND_CHILD1, grandChild1Def, child1);
        child1.addComponent(grandChild1);
        ComponentDefinition<CompositeImplementation> grandGrandChild1Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalComponent<?> grandGrandChild1 = new LogicalComponent(GRAND_GRAND_CHILD, grandGrandChild1Def, domain);
        grandChild1.addComponent(grandGrandChild1);
        ComponentDefinition<CompositeImplementation> child2Def = new ComponentDefinition<CompositeImplementation>("child1", null);
        LogicalCompositeComponent child2 = new LogicalCompositeComponent(CHILD2, child2Def, domain);
        domain.addComponent(child2);

        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain).atLeastOnce();
        EasyMock.replay(lcm);
        mBean = new DistributedDomainMBean(null, null, lcm, info, null);
    }
}
