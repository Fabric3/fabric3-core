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
package org.fabric3.fabric.domain.collector;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.spi.model.instance.LogicalBindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 *
 */
public class CollectorImplTestCase extends TestCase {
    private static final URI CONTRIBUTION_1 = URI.create("deployable");
    private static final URI CONTRIBUTION_2 = URI.create("deployable2");

    private Collector collector;

    public void testMarkAsProvisioned() {

        LogicalCompositeComponent domain = createDomain(LogicalState.NEW);

        collector.markAsProvisioned(domain);

        for (LogicalComponent<?> component : domain.getComponents()) {
            assertEquals(LogicalState.PROVISIONED, component.getState());
            for (LogicalService service : component.getServices()) {
                for (LogicalBinding<?> binding : service.getBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
                for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
            }
            for (LogicalReference reference : component.getReferences()) {
                for (LogicalBinding<?> binding : reference.getBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
                for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            assertEquals(LogicalState.PROVISIONED, channel.getState());
        }

        for (List<LogicalWire> wireList : domain.getWires().values()) {
            for (LogicalWire wire : wireList) {
                assertEquals(LogicalState.PROVISIONED, wire.getState());
            }
        }

        for (LogicalResource<?> resource : domain.getResources()) {
            assertEquals(LogicalState.PROVISIONED, resource.getState());
        }

    }

    public void testNoMarkAsProvisioned() {
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI child1Uri = URI.create("child1");
        createComponent(child1Uri, CONTRIBUTION_1, LogicalState.MARKED, domain);
        URI child2Uri = URI.create("child2");
        createComponent(child2Uri, CONTRIBUTION_1, LogicalState.MARKED, domain);
        createChannel(URI.create("channel"), CONTRIBUTION_1, LogicalState.MARKED, domain);
        createWire(child1Uri, child2Uri, CONTRIBUTION_1, LogicalState.MARKED, domain);
        createResource(CONTRIBUTION_1, LogicalState.MARKED, domain);

        collector.markAsProvisioned(domain);

        for (LogicalComponent<?> component : domain.getComponents()) {
            assertEquals(LogicalState.MARKED, component.getState());
            for (LogicalService service : component.getServices()) {
                for (LogicalBinding<?> binding : service.getBindings()) {
                    assertEquals(LogicalState.MARKED, binding.getState());
                }
                for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                    assertEquals(LogicalState.MARKED, binding.getState());
                }
            }
            for (LogicalReference reference : component.getReferences()) {
                for (LogicalBinding<?> binding : reference.getBindings()) {
                    assertEquals(LogicalState.MARKED, binding.getState());
                }
                for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                    assertEquals(LogicalState.MARKED, binding.getState());
                }
            }
        }

        for (LogicalChannel channel : domain.getChannels()) {
            assertEquals(LogicalState.MARKED, channel.getState());
            for (LogicalBinding<?> binding : channel.getBindings()) {
                assertEquals(LogicalState.MARKED, binding.getState());
            }
        }

        for (List<LogicalWire> wires : domain.getWires().values()) {
            for (LogicalWire wire : wires) {
                assertEquals(LogicalState.MARKED, wire.getState());
            }
        }

    }

    public void testMarkNewBindingAsProvisioned() {
        // mark a binding deployed in a different composite as provisioned
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI child1Uri = URI.create("child1");
        createComponent(child1Uri, CONTRIBUTION_1, LogicalState.NEW, domain);
        domain.getComponents().iterator().next().setState(LogicalState.PROVISIONED);

        collector.markAsProvisioned(domain);

        for (LogicalComponent<?> component : domain.getComponents()) {
            for (LogicalService service : component.getServices()) {
                for (LogicalBinding<?> binding : service.getBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
                for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }

            }
            for (LogicalReference reference : component.getReferences()) {
                for (LogicalBinding<?> binding : reference.getBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
                for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                    assertEquals(LogicalState.PROVISIONED, binding.getState());
                }
            }
        }
    }

    public void testMarkNewWireAsProvisioned() {
        // mark a binding deployed in a different composite as provisioned
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI child1Uri = URI.create("child1");
        createComponent(child1Uri, CONTRIBUTION_1, LogicalState.PROVISIONED, domain);
        URI child2Uri = URI.create("child2");
        createComponent(child2Uri, CONTRIBUTION_1, LogicalState.PROVISIONED, domain);
        createWire(child1Uri, child2Uri, CONTRIBUTION_1, LogicalState.NEW, domain);

        collector.markAsProvisioned(domain);

        for (List<LogicalWire> wires : domain.getWires().values()) {
            for (LogicalWire wire : wires) {
                assertEquals(LogicalState.PROVISIONED, wire.getState());
            }
        }
    }

    public void testMarkAndCollect() {

        LogicalCompositeComponent domain = createDomain(LogicalState.PROVISIONED);

        collector.markForCollection(CONTRIBUTION_1, domain);

        for (LogicalComponent component : domain.getComponents()) {
            if (CONTRIBUTION_1.equals(component.getDefinition().getContributionUri())) {
                assertEquals(LogicalState.MARKED, component.getState());
            } else {
                assertEquals(LogicalState.PROVISIONED, component.getState());
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            if (CONTRIBUTION_1.equals(channel.getDefinition().getContributionUri())) {
                assertEquals(LogicalState.MARKED, channel.getState());
            } else {
                assertEquals(LogicalState.PROVISIONED, channel.getState());
            }
        }

        collector.collect(domain);

        verifyDeployable1Collected(domain);

        assertEquals(1, domain.getComponents().size());
        assertEquals(1, domain.getChannels().size());
        assertEquals(1, domain.getWires().size());
        assertEquals(1, domain.getResources().size());
    }

    public <I extends Implementation<?>> void testMarkAndCollectBindings() {
        // verify bindings deployed in a different composite can be marked and collected
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);
        URI child1Uri = URI.create("child1");

        LogicalComponent<I> component = new LogicalComponent<>(child1Uri, new Component<>("child1"), domain);
        component.setState(LogicalState.PROVISIONED);

        LogicalService service = new LogicalService(URI.create("child1#service"), null, component);
        createBinding(service, CONTRIBUTION_2, LogicalState.PROVISIONED);
        createCallbackBinding(service, CONTRIBUTION_2, LogicalState.PROVISIONED);
        component.addService(service);

        domain.addComponent(component);

        collector.markForCollection(CONTRIBUTION_2, domain);

        for (LogicalComponent<?> child : domain.getComponents()) {
            for (LogicalService childService : child.getServices()) {
                for (LogicalBinding<?> binding : childService.getBindings()) {
                    assertEquals(LogicalState.MARKED, binding.getState());
                }
                for (LogicalBinding<?> binding : childService.getCallbackBindings()) {
                    assertEquals(LogicalState.MARKED, binding.getState());
                }

            }
        }

        collector.collect(domain);

        for (LogicalComponent<?> child : domain.getComponents()) {
            for (LogicalService childService : child.getServices()) {
                assertTrue(childService.getBindings().isEmpty());
                assertTrue(childService.getCallbackBindings().isEmpty());
            }
        }

    }

    public void testMarkAndCollectWires() {
        // verify a wire deployed in a different composite can be marked and collected
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        createComponent(child1Uri, CONTRIBUTION_1, LogicalState.PROVISIONED, domain);
        URI child2Uri = URI.create("child2");
        createComponent(child2Uri, CONTRIBUTION_1, LogicalState.PROVISIONED, domain);
        createWire(child1Uri, child2Uri, CONTRIBUTION_2, LogicalState.PROVISIONED, domain);

        collector.markForCollection(CONTRIBUTION_2, domain);

        for (List<LogicalWire> wireList : domain.getWires().values()) {
            for (LogicalWire wire : wireList) {
                assertEquals(LogicalState.MARKED, wire.getState());
            }
        }

        collector.collect(domain);
        assertTrue(domain.getWires().isEmpty());
    }

    private void verifyDeployable1Collected(LogicalCompositeComponent domain) {
        for (LogicalComponent<?> component : domain.getComponents()) {
            if (CONTRIBUTION_1.equals(component.getDefinition().getContributionUri())) {
                fail("Component not collected: " + component.getUri());
            }
            for (LogicalService service : component.getServices()) {
                for (LogicalBinding<?> binding : service.getBindings()) {
                    if (CONTRIBUTION_1.equals(binding.getTargetContribution())) {
                        fail("Binding on service not collected: " + service.getUri());
                    }
                }
                for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                    if (CONTRIBUTION_1.equals(binding.getTargetContribution())) {
                        fail("Binding on service not collected: " + service.getUri());
                    }
                }
            }
            for (LogicalReference reference : component.getReferences()) {
                for (LogicalBinding<?> binding : reference.getBindings()) {
                    if (CONTRIBUTION_1.equals(binding.getTargetContribution())) {
                        fail("Binding on reference not collected: " + reference.getUri());
                    }
                }
                for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                    if (CONTRIBUTION_1.equals(binding.getTargetContribution())) {
                        fail("Binding on reference not collected: " + reference.getUri());
                    }
                }
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            if (CONTRIBUTION_1.equals(channel.getDefinition().getContributionUri())) {
                fail("Component not collected: " + channel.getUri());
            }
            for (LogicalBinding<?> binding : channel.getBindings()) {
                if (CONTRIBUTION_1.equals(binding.getTargetContribution())) {
                    fail("Binding on channel not collected: " + channel.getUri());
                }
            }
        }

        Collection<List<LogicalWire>> wireList = domain.getWires().values();
        for (List<LogicalWire> wires : wireList) {
            for (LogicalWire wire : wires) {
                if (CONTRIBUTION_1.equals(wire.getTargetContribution())) {
                    fail("Wire not collected: " + wire.getSource().getUri());
                }
            }
        }

        for (LogicalResource<?> resource : domain.getResources()) {
            if (CONTRIBUTION_1.equals(resource.getDefinition().getContributionUri())) {
                fail("Resource not collected");
            }
        }
    }

    private LogicalCompositeComponent createDomain(LogicalState state) {
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        createComponent(child1Uri, CONTRIBUTION_1, state, domain);

        URI child2Uri = URI.create("child2");
        createComponent(child2Uri, CONTRIBUTION_2, state, domain);

        URI childChannel1 = URI.create("childChannel1");
        createChannel(childChannel1, CONTRIBUTION_1, state, domain);

        URI childChannel2 = URI.create("childChannel2");
        createChannel(childChannel2, CONTRIBUTION_2, state, domain);

        URI childCompositeUri = URI.create("childComposite");
        LogicalCompositeComponent childComposite = new LogicalCompositeComponent(childCompositeUri, new Component<>("childComposite"), domain);
        childComposite.setState(state);
        childComposite.getDefinition().setContributionUri(CONTRIBUTION_1);
        domain.addComponent(childComposite);

        URI child3Uri = URI.create("child3");
        createComponent(child3Uri, CONTRIBUTION_1, state, childComposite);

        createWire(child1Uri, child2Uri, CONTRIBUTION_1, state, domain);
        createWire(child2Uri, child2Uri, CONTRIBUTION_2, state, domain);

        createResource(CONTRIBUTION_1, state, domain);
        createResource(CONTRIBUTION_2, state, domain);
        return domain;
    }

    private void createResource(URI contributionUri, LogicalState state, LogicalCompositeComponent parent) {
        LogicalResource resource = new LogicalResource<>(new Resource(), parent);
        resource.getDefinition().setContributionUri(contributionUri);
        resource.setState(state);
        parent.addResource(resource);
    }

    private void createWire(URI sourceUri, URI targetUri, URI contributionUri, LogicalState state, LogicalCompositeComponent domain) {
        LogicalReference source = domain.getComponent(sourceUri).getReference("reference");
        LogicalService target = domain.getComponent(targetUri).getService("service");
        LogicalWire wire = new LogicalWire(domain, source, target, contributionUri);
        wire.setState(state);
        domain.addWire(source, wire);
    }

    private void createChannel(URI childChannel1, URI contributionUri, LogicalState state, LogicalCompositeComponent domain) {
        LogicalChannel channel = new LogicalChannel(childChannel1, new Channel("channel"), domain);
        channel.getDefinition().setContributionUri(contributionUri);
        channel.setState(state);
        createBinding(channel, contributionUri, state);
        domain.addChannel(channel);
    }

    private <I extends Implementation<?>> void createComponent(URI uri, URI contributionUri, LogicalState state, LogicalCompositeComponent parent) {
        LogicalComponent<I> component = new LogicalComponent<>(uri, new Component<>("component"), parent);
        component.setState(state);
        component.getDefinition().setContributionUri(contributionUri);

        LogicalService service = new LogicalService(URI.create(uri.toString() + "#service"), null, component);
        createBinding(service, contributionUri, state);
        createCallbackBinding(service, contributionUri, state);
        component.addService(service);

        LogicalReference reference = new LogicalReference(URI.create(uri.toString() + "#reference"), null, component);
        createBinding(reference, contributionUri, state);
        createCallbackBinding(reference, contributionUri, state);
        component.addReference(reference);

        parent.addComponent(component);
    }

    @SuppressWarnings({"unchecked"})
    private void createBinding(LogicalBindable bindable, URI contributionUri, LogicalState state) {
        LogicalBinding binding = new LogicalBinding(null, bindable, contributionUri);
        binding.setState(state);
        bindable.addBinding(binding);
    }

    @SuppressWarnings({"unchecked"})
    private void createCallbackBinding(LogicalBindable bindable, URI contributionUri, LogicalState state) {
        LogicalBinding binding = new LogicalBinding(null, bindable, contributionUri);
        binding.setCallback(true);
        binding.setState(state);
        bindable.addCallbackBinding(binding);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        collector = new CollectorImpl();
    }
}
