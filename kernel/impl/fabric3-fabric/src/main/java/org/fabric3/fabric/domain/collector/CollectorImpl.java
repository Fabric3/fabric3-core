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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import static org.fabric3.spi.model.instance.LogicalState.MARKED;
import static org.fabric3.spi.model.instance.LogicalState.NEW;
import static org.fabric3.spi.model.instance.LogicalState.PROVISIONED;

/**
 * Default Collector implementation.
 */
public class CollectorImpl implements Collector {

    public void markAsProvisioned(LogicalCompositeComponent composite) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            // note: all components must be traversed as new wires could be deployed to an existing provisioned component
            if (component instanceof LogicalCompositeComponent) {
                markAsProvisioned((LogicalCompositeComponent) component);
            }
            if (NEW == component.getState()) {
                component.setState(PROVISIONED);
            }
            for (LogicalService service : component.getServices()) {
                service.getBindings().stream().filter(binding -> NEW == binding.getState()).forEach(binding -> binding.setState(PROVISIONED));
                service.getCallbackBindings().stream().filter(binding -> NEW == binding.getState()).forEach(binding -> binding.setState(PROVISIONED));
            }
            for (LogicalReference reference : component.getReferences()) {
                reference.getBindings().stream().filter(binding -> NEW == binding.getState()).forEach(binding -> binding.setState(PROVISIONED));
                reference.getCallbackBindings().stream().filter(binding -> NEW == binding.getState()).forEach(binding -> binding.setState(PROVISIONED));
            }
        }
        for (List<LogicalWire> wires : composite.getWires().values()) {
            wires.stream().filter(wire -> NEW == wire.getState()).forEach(wire -> wire.setState(PROVISIONED));
        }
        for (LogicalChannel channel : composite.getChannels()) {
            if (NEW == channel.getState()) {
                channel.setState(PROVISIONED);
            }
            LogicalBinding<?> binding = channel.getBinding();
            if (binding != null && NEW == binding.getState()) {
                binding.setState(PROVISIONED);
            }
        }
        composite.getResources().stream().filter(resource -> NEW == resource.getState()).forEach(resource -> resource.setState(PROVISIONED));

    }

    public void markForCollection(URI contributionUri, LogicalCompositeComponent composite) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            if (contributionUri.equals(component.getDefinition().getContributionUri())) {
                if (component.getDefinition() != null && !contributionUri.equals(component.getDefinition().getContributionUri())) {
                    continue; // composite is not part of the contribution being undeployed
                }
                if (component instanceof LogicalCompositeComponent) {
                    markForCollection(contributionUri, (LogicalCompositeComponent) component);
                }
                component.setState(MARKED);
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        binding.setState(MARKED);
                    }
                }
                for (LogicalReference reference : component.getReferences()) {
                    for (LogicalBinding<?> binding : reference.getBindings()) {
                        binding.setState(MARKED);
                    }
                    for (LogicalWire wire : composite.getWires(reference)) {
                        wire.setState(MARKED);
                    }
                }

            } else {
                // mark service and callback bindings that were dynamically added to satisfy a wire when the deployable was provisioned
                for (LogicalService service : component.getServices()) {
                    service.getBindings().stream().filter(binding -> contributionUri.equals(binding.getTargetContribution())).forEach(binding -> binding.setState(
                            MARKED));
                    service.getCallbackBindings().stream().filter(binding -> contributionUri.equals(binding.getTargetContribution())).forEach(binding -> binding.setState(
                            MARKED));
                }
                // recurse through wires and mark any that were part of the deployable being undeployed
                // this can occur when a wire is configured in a deployable other than its source component
                for (List<LogicalWire> wires : composite.getWires().values()) {
                    for (LogicalWire wire : wires) {
                        if (MARKED == wire.getState()) {
                            continue;
                        }
                        if (contributionUri.equals(wire.getTargetContribution())) {
                            wire.setState(MARKED);
                        }
                    }
                }
            }
        }
        for (LogicalChannel channel : composite.getChannels()) {
            if (channel.getDefinition() != null && !contributionUri.equals(channel.getDefinition().getContributionUri())) {
                continue; // composite is not part of the contribution being undeployed
            }
            if (contributionUri.equals(channel.getDefinition().getContributionUri())) {
                channel.setState(MARKED);
            }
            LogicalBinding<?> binding = channel.getBinding();
            if (binding != null && contributionUri.equals(binding.getTargetContribution())) {
                binding.setState(MARKED);
            }
        }

        for (LogicalResource resource : composite.getResources()) {
            if (contributionUri.equals(resource.getDefinition().getContributionUri())) {
                resource.setState(MARKED);
            }
        }
    }

    public void collect(LogicalCompositeComponent composite) {
        Iterator<LogicalComponent<?>> iter = composite.getComponents().iterator();
        while (iter.hasNext()) {
            LogicalComponent<?> component = iter.next();
            if (MARKED == component.getState()) {
                iter.remove();
            } else {
                for (LogicalService service : component.getServices()) {
                    removeMarkedBindings(service.getBindings().iterator());
                    removeMarkedBindings(service.getCallbackBindings().iterator());
                }
                for (LogicalReference reference : component.getReferences()) {
                    removeMarkedBindings(reference.getBindings().iterator());
                    removeMarkedBindings(reference.getCallbackBindings().iterator());
                }
                if (component instanceof LogicalCompositeComponent) {
                    collect((LogicalCompositeComponent) component);
                }
            }
        }
        List<LogicalReference> toRemove = new ArrayList<>();
        for (Map.Entry<LogicalReference, List<LogicalWire>> wires : composite.getWires().entrySet()) {
            for (Iterator<LogicalWire> it = wires.getValue().iterator(); it.hasNext(); ) {
                LogicalWire wire = it.next();
                if (MARKED == wire.getState()) {
                    it.remove();
                }
            }
            if (wires.getValue().isEmpty()) {
                toRemove.add(wires.getKey());
            }
        }
        // cleanup reference entries that have no wires
        for (LogicalReference reference : toRemove) {
            composite.getWires().remove(reference);
        }

        Iterator<LogicalChannel> channelIter = composite.getChannels().iterator();
        while (channelIter.hasNext()) {
            LogicalChannel channel = channelIter.next();
            if (MARKED == channel.getState()) {
                channelIter.remove();
            }
        }
        Iterator<LogicalResource<?>> resourceIter = composite.getResources().iterator();
        while (resourceIter.hasNext()) {
            LogicalResource<?> resource = resourceIter.next();
            if (MARKED == resource.getState()) {
                resourceIter.remove();
            }
        }
    }

    /**
     * Removes marked bindings
     *
     * @param iter the collection of bindings to iterate
     */
    private void removeMarkedBindings(Iterator<LogicalBinding<?>> iter) {
        while (iter.hasNext()) {
            LogicalBinding<?> binding = iter.next();
            if (MARKED == binding.getState()) {
                iter.remove();
            }
        }
    }

}
