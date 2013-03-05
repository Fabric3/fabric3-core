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
package org.fabric3.policy.resolver;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 */
public class AbstractPolicyResolverTestCase extends TestCase {
    private MockResolver resolver = new MockResolver(null, null, null);

    public void testAggregatedProfileIntents() throws Exception {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("parent"), null, null);
        LogicalCompositeComponent child = new LogicalCompositeComponent(URI.create("child"), null, parent);
        parent.addComponent(child);
        QName barIntent = new QName("foo", "bar");
        QName bazIntent = new QName("foo", "baz");
        parent.addIntent(barIntent);
        child.addIntent(bazIntent);

        Set<QName> intents = resolver.aggregateIntents(child);

        assertEquals(2, intents.size());
        assertTrue(intents.contains(barIntent));
        assertTrue(intents.contains(bazIntent));
    }

    public void testAggregatedQualifiedIntents() throws Exception {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("parent"), null, null);
        LogicalCompositeComponent child = new LogicalCompositeComponent(URI.create("child"), null, parent);
        parent.addComponent(child);
        QName barIntent = new QName("foo", "foo.bar");
        QName bazIntent = new QName("foo", "foo.baz");
        parent.addIntent(barIntent);
        child.addIntent(bazIntent);

        Set<QName> intents = resolver.aggregateIntents(child);

        assertEquals(2, intents.size());
        assertTrue(intents.contains(barIntent));
        assertTrue(intents.contains(bazIntent));
    }

    public void testAggregateIntentsQualifiedIntentOnParent() throws Exception {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("parent"), null, null);
        LogicalCompositeComponent child = new LogicalCompositeComponent(URI.create("child"), null, parent);
        parent.addComponent(child);
        QName profileIntent = new QName("foo", "foo");
        QName qualifiedIntent = new QName("foo", "foo.baz");
        parent.addIntent(qualifiedIntent);
        child.addIntent(profileIntent);

        Set<QName> intents = resolver.aggregateIntents(child);

        assertEquals(1, intents.size());
        assertTrue(intents.contains(qualifiedIntent));
    }

    public void testAggregateIntentsQualifiedIntentOnChild() throws Exception {
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("parent"), null, null);
        LogicalCompositeComponent child = new LogicalCompositeComponent(URI.create("child"), null, parent);
        parent.addComponent(child);
        QName profileIntent = new QName("foo", "foo");
        QName qualifiedIntent = new QName("foo", "foo.baz");
        parent.addIntent(profileIntent);
        child.addIntent(qualifiedIntent);

        Set<QName> intents = resolver.aggregateIntents(child);

        assertEquals(1, intents.size());
        assertTrue(intents.contains(qualifiedIntent));
    }

    public void testFilterMutuallyExclusiveIntents() throws Exception {
        Set<Intent> intents = new LinkedHashSet<Intent>();
        QName intent1Name = new QName("foo", "bar");
        QName intent2Name = new QName("foo", "baz");
        Intent intent1 = new Intent(intent1Name, null, null, null, true, Collections.singleton(intent2Name), null, false);
        Intent intent2 = new Intent(intent2Name, null, null, null, true, Collections.singleton(intent1Name), null, false);
        intents.add(intent1);
        intents.add(intent2);

        resolver.filterMutuallyExclusiveIntents(intents);

        assertEquals(1, intents.size());
        assertTrue(intents.contains(intent1));
    }

    private class MockResolver extends AbstractPolicyResolver {

        protected MockResolver(PolicyRegistry policyRegistry, LogicalComponentManager lcm, PolicyEvaluator policyEvaluator) {
            super(policyRegistry, lcm, policyEvaluator);
        }

        public Set<QName> aggregateIntents(LogicalBinding<?> scaArtifact) {
            return super.aggregateIntents(scaArtifact);
        }

        protected void filterMutuallyExclusiveIntents(Set<Intent> intents) {
            super.filterMutuallyExclusiveIntents(intents);
        }
    }
}
