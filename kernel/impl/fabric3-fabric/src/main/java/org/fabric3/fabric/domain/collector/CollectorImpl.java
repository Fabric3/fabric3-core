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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

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
 * Default Collector implementation.
 */
public class CollectorImpl implements Collector {

    public void markAsProvisioned(LogicalCompositeComponent composite) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            // note: all components must be traversed as new wires could be deployed to an existing provisioned component
            if (component instanceof LogicalCompositeComponent) {
                markAsProvisioned((LogicalCompositeComponent) component);
            }
            if (LogicalState.NEW == component.getState()) {
                component.setState(LogicalState.PROVISIONED);
            }
            for (LogicalService service : component.getServices()) {
                for (LogicalBinding<?> binding : service.getBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
                for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
            }
            for (LogicalReference reference : component.getReferences()) {
                for (LogicalBinding<?> binding : reference.getBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
                for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
            }
        }
        for (List<LogicalWire> wires : composite.getWires().values()) {
            for (LogicalWire wire : wires) {
                if (LogicalState.NEW == wire.getState()) {
                    wire.setState(LogicalState.PROVISIONED);
                }
            }
        }
        for (LogicalChannel channel : composite.getChannels()) {
            if (LogicalState.NEW == channel.getState()) {
                channel.setState(LogicalState.PROVISIONED);
            }
            LogicalBinding<?> binding = channel.getBinding();
            if (binding != null && LogicalState.NEW == binding.getState()) {
                binding.setState(LogicalState.PROVISIONED);
            }
        }
        for (LogicalResource resource : composite.getResources()) {
            if (LogicalState.NEW == resource.getState()) {
                resource.setState(LogicalState.PROVISIONED);
            }
        }

    }


    public void markForCollection(QName deployable, LogicalCompositeComponent composite) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            if (deployable.equals(component.getDeployable())) {
                if (component instanceof LogicalCompositeComponent) {
                    markForCollection(deployable, (LogicalCompositeComponent) component);
                }
                component.setState(LogicalState.MARKED);
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        binding.setState(LogicalState.MARKED);
                    }
                }
                for (LogicalReference reference : component.getReferences()) {
                    for (LogicalBinding<?> binding : reference.getBindings()) {
                        binding.setState(LogicalState.MARKED);
                    }
                    for (LogicalWire wire : composite.getWires(reference)) {
                        wire.setState(LogicalState.MARKED);
                    }
                }

            } else {
                // mark service and callback bindings that were dynamically added to satisfy a wire when the deployable was provisioned
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        if (deployable.equals(binding.getDeployable())) {
                            binding.setState(LogicalState.MARKED);
                        }
                    }
                    for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                        if (deployable.equals(binding.getDeployable())) {
                            binding.setState(LogicalState.MARKED);
                        }
                    }
                }
                // recurse through wires and mark any that were part of the deployable being undeployed
                // this can occur when a wire is configured in a deployable other than its source component
                for (List<LogicalWire> wires : composite.getWires().values()) {
                    for (LogicalWire wire : wires) {
                        if (LogicalState.MARKED == wire.getState()) {
                            continue;
                        }
                        if (deployable.equals(wire.getTargetDeployable())) {
                            wire.setState(LogicalState.MARKED);
                        }
                    }
                }
            }
        }
        for (LogicalChannel channel : composite.getChannels()) {
            if (deployable.equals(channel.getDeployable())) {
                channel.setState(LogicalState.MARKED);
            }
            LogicalBinding<?> binding = channel.getBinding();
            if (binding != null && deployable.equals(binding.getDeployable())) {
                binding.setState(LogicalState.MARKED);
            }
        }

        for (LogicalResource resource : composite.getResources()) {
            if (deployable.equals(resource.getDeployable())) {
                resource.setState(LogicalState.MARKED);
            }
        }
    }

    public void collect(LogicalCompositeComponent composite) {
        Iterator<LogicalComponent<?>> iter = composite.getComponents().iterator();
        while (iter.hasNext()) {
            LogicalComponent<?> component = iter.next();
            if (LogicalState.MARKED == component.getState()) {
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
            for (Iterator<LogicalWire> it = wires.getValue().iterator(); it.hasNext();) {
                LogicalWire wire = it.next();
                if (LogicalState.MARKED == wire.getState()) {
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
            if (LogicalState.MARKED == channel.getState()) {
                channelIter.remove();
            }
        }
        Iterator<LogicalResource<?>> resourceIter = composite.getResources().iterator();
        while (resourceIter.hasNext()) {
            LogicalResource<?> resource = resourceIter.next();
            if (LogicalState.MARKED == resource.getState()) {
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
            if (LogicalState.MARKED == binding.getState()) {
                iter.remove();
            }
        }
    }


}
