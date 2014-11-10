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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.policy.resolver;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.domain.LogicalComponentManager;
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
        Set<Intent> intents = new LinkedHashSet<>();
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

        public Set<QName> aggregateIntents(LogicalBinding<?> scaArtifact) throws PolicyResolutionException {
            return super.aggregateIntents(scaArtifact);
        }

        protected void filterMutuallyExclusiveIntents(Set<Intent> intents) {
            super.filterMutuallyExclusiveIntents(intents);
        }
    }
}
