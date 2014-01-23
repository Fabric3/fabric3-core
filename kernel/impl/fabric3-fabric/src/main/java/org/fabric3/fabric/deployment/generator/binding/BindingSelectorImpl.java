/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.fabric.deployment.generator.binding;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.deployment.generator.binding.BindingMatchResult;
import org.fabric3.spi.deployment.generator.binding.BindingProvider;
import org.fabric3.spi.deployment.generator.binding.BindingSelectionException;
import org.fabric3.spi.deployment.generator.binding.BindingSelectionStrategy;
import org.fabric3.spi.deployment.generator.binding.BindingSelector;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.remote.RemoteImplementation;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.host.Names.LOCAL_ZONE;

/**
 * Selects a binding provider by delegating to a BindingSelectionStrategy configured for the domain. For each wire, if a remote service has an explicit binding,
 * its configuration will be used to construct the reference binding. If a service does not have an explicit binding, the wire uses binding.sca. The
 * BindingSelector will select an appropriate remote transport and create binding configuration for both sides of the wire.
 */
@EagerInit
public class BindingSelectorImpl implements BindingSelector {
    private boolean disable;
    private HostInfo info;
    private BindingSelectionStrategy strategy;
    private List<BindingProvider> providers = new ArrayList<BindingProvider>();

    public BindingSelectorImpl(@Reference HostInfo info) {
        this.info = info;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:bindings/f3:binding.sca/@disable")
    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    /**
     * Lazily injects providers as they become available from runtime extensions.
     *
     * @param providers the set of providers
     */
    @Reference(required = false)
    public void setProviders(List<BindingProvider> providers) {
        this.providers = providers;
        orderProviders();
    }

    @Reference(required = false)
    public void setStrategy(BindingSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    @Init
    public void orderProviders() {
        if (strategy != null) {
            strategy.order(providers);
        }
    }

    public void selectBindings(LogicalCompositeComponent domain) throws BindingSelectionException {
        if ((RuntimeMode.CONTROLLER != info.getRuntimeMode() && RuntimeMode.NODE != info.getRuntimeMode()) || disable) {
            // there are no remote wires when the domain is contained withing a single VM (including Participant mode, which has a runtime domain)
            return;
        }
        Collection<LogicalComponent<?>> components = domain.getComponents();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW) {
                selectBindings(component);
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            selectBinding(channel);
        }
    }

    public void selectBinding(LogicalWire wire) throws BindingSelectionException {
        List<BindingMatchResult> results = new ArrayList<BindingMatchResult>();
        LogicalReference source = wire.getSource();
        LogicalService target = wire.getTarget();
        for (BindingProvider provider : providers) {
            BindingMatchResult result = provider.canBind(wire);
            if (result.isMatch()) {
                // clear binding.sca
                source.getBindings().clear();
                target.getBindings().clear();
                provider.bind(wire);
                if (source.getLeafReference().getBindings().isEmpty()) {
                    QName type = result.getType();
                    throw new BindingSelectionException("Binding provider error. Provider did not set a binding for the reference: " + type);
                }
                wire.setSourceBinding(source.getBindings().get(0));
                if (!(target.getParent().getDefinition().getImplementation() instanceof RemoteImplementation)) {
                    if (target.getLeafService().getBindings().isEmpty()) {
                        QName type = result.getType();
                        throw new BindingSelectionException("Binding provider error. Provider did not set a binding for the service: " + type);
                    }
                    if (!target.getBindings().isEmpty()) {
                        wire.setTargetBinding(target.getBindings().get(0));
                    } else {
                        wire.setTargetBinding(target.getLeafService().getBindings().get(0));
                    }
                }
                return;
            }
            results.add(result);

        }
        URI sourceUri = source.getUri();
        URI targetUri = target.getUri();
        throw new NoSCABindingProviderException("No SCA binding provider suitable for creating wire from " + sourceUri + " to " + targetUri, results);
    }

    /**
     * Selects and configures bindings for wires sourced from the given component.
     *
     * @param component the component
     * @throws BindingSelectionException if an error occurs selecting a binding
     */
    private void selectBindings(LogicalComponent<?> component) throws BindingSelectionException {
        // bind remote wires
        for (LogicalReference reference : component.getReferences()) {
            for (LogicalWire wire : reference.getWires()) {
                LogicalService targetService = wire.getTarget();
                if (targetService != null) {
                    LogicalComponent<?> targetComponent = targetService.getParent();
                    if ((LOCAL_ZONE.equals(component.getZone()) && LOCAL_ZONE.equals(targetComponent.getZone()))) {
                        // components are local, no need for a binding
                        continue;
                    } else if (!LOCAL_ZONE.equals(component.getZone()) && component.getZone().equals(targetComponent.getZone())) {
                        // components are local, no need for a binding
                        continue;
                    }
                    selectBinding(wire);
                }
            }
        }

        // on a node runtime bind all domain level, remotable services that are not explicitly configured with a binding
        if (RuntimeMode.NODE == info.getRuntimeMode()) {
            for (LogicalService service : component.getServices()) {
                if (bindService(service)) {
                    for (BindingProvider provider : providers) {
                        BindingMatchResult result = provider.canBind(service);
                        if (result.isMatch()) {
                            provider.bind(service);
                        }
                    }
                }
            }
        }
    }

    /**
     * Selects and configures a binding for a channel.
     *
     * @param channel the channel
     * @throws BindingSelectionException if an error occurs selecting a binding
     */
    private void selectBinding(LogicalChannel channel) throws BindingSelectionException {
        if (channel.isConcreteBound() || channel.getDefinition().isLocal()) {
            return;
        }
        List<BindingMatchResult> results = new ArrayList<BindingMatchResult>();
        for (BindingProvider provider : providers) {
            BindingMatchResult result = provider.canBind(channel);
            if (result.isMatch()) {
                // clear binding.sca
                channel.clearBinding();
                provider.bind(channel);
                if (channel.getBindings().isEmpty()) {
                    QName type = result.getType();
                    throw new BindingSelectionException("Binding provider error. Provider did not set a binding for the channel: " + type);
                }
                return;
            }
            results.add(result);
        }
        URI uri = channel.getUri();
        throw new NoSCABindingProviderException("No SCA binding provider suitable for channel " + uri, results);
    }

    /**
     * Determines if the service should be bound, i.e. if it has not already been bound by binding.sca or is remote (and not hosted on the current runtime).
     *
     * @param service the target
     * @return true if the target should be bound
     */
    private boolean bindService(LogicalService service) {
        if (!service.getBindings().isEmpty() || !service.getLeafService().getServiceContract().isRemotable()) {
            return false;
        }
        if (service.getServiceContract() instanceof RemoteServiceContract) {
            return false;
        }
        for (LogicalBinding<?> binding : service.getBindings()) {
            if (binding.isAssigned()) {
                return false;
            }
        }
        return true;
    }

}

