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
package org.fabric3.fabric.collector;

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
        List<LogicalReference> toRemove = new ArrayList<LogicalReference>();
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
