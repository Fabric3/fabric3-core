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
package org.fabric3.fabric.collector;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * @version $Rev$ $Date$
 */
public class CollectorImplTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName(null, "deployable");

    private Collector collector;

    public <I extends Implementation<?>> void testMarkAndCollect() {

        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        LogicalComponent<I> child1 = new LogicalComponent<I>(child1Uri, null, domain);
        child1.setState(LogicalState.PROVISIONED);
        child1.setDeployable(DEPLOYABLE);
        URI child2Uri = URI.create("child2");
        LogicalComponent<I> child2 = new LogicalComponent<I>(child2Uri, null, domain);
        child2.setState(LogicalState.PROVISIONED);

        URI childCompositeUri = URI.create("childComposite");
        LogicalCompositeComponent childComposite = new LogicalCompositeComponent(childCompositeUri, null, domain);
        childComposite.setState(LogicalState.PROVISIONED);
        childComposite.setDeployable(DEPLOYABLE);
        URI child3Uri = URI.create("child3");
        LogicalComponent<I> child3 = new LogicalComponent<I>(child3Uri, null, childComposite);
        child3.setState(LogicalState.PROVISIONED);
        child3.setDeployable(DEPLOYABLE);
        childComposite.addComponent(child3);

        domain.addComponent(child1);
        domain.addComponent(child2);
        domain.addComponent(childComposite);

        collector.markForCollection(DEPLOYABLE, domain);

        assertEquals(LogicalState.MARKED, childComposite.getState());
        assertEquals(LogicalState.MARKED, child1.getState());
        assertEquals(LogicalState.MARKED, child3.getState());
        assertEquals(LogicalState.PROVISIONED, child2.getState());

        collector.collect(domain);

        assertNull(domain.getComponent(child1Uri));
        assertNotNull(domain.getComponent(child2Uri));
        assertNull(domain.getComponent(childCompositeUri));

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        collector = new CollectorImpl();
    }
}
