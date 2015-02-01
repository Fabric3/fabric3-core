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
package org.fabric3.fabric.domain.generator.binding;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.domain.generator.binding.BindingMatchResult;
import org.fabric3.spi.domain.generator.binding.BindingProvider;
import org.fabric3.spi.domain.generator.binding.BindingSelectionStrategy;
import org.fabric3.spi.domain.generator.binding.BindingSelector;
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
    private List<BindingProvider> providers = new ArrayList<>();

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

    public void selectBindings(LogicalCompositeComponent domain) throws Fabric3Exception {
        if (RuntimeMode.NODE != info.getRuntimeMode() || disable) {
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

    public void selectBinding(LogicalWire wire) throws Fabric3Exception {
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
                    throw new Fabric3Exception("Binding provider error. Provider did not set a binding for the reference: " + type);
                }
                wire.setSourceBinding(source.getBindings().get(0));
                if (!(target.getParent().getDefinition().getImplementation() instanceof RemoteImplementation)) {
                    if (target.getLeafService().getBindings().isEmpty()) {
                        QName type = result.getType();
                        throw new Fabric3Exception("Binding provider error. Provider did not set a binding for the service: " + type);
                    }
                    if (!target.getBindings().isEmpty()) {
                        wire.setTargetBinding(target.getBindings().get(0));
                    } else {
                        wire.setTargetBinding(target.getLeafService().getBindings().get(0));
                    }
                }
                return;
            }
        }
        URI sourceUri = source.getUri();
        URI targetUri = target.getUri();
        throw new Fabric3Exception("No SCA binding provider suitable for creating wire from " + sourceUri + " to " + targetUri);
    }

    /**
     * Selects and configures bindings for wires sourced from the given component.
     *
     * @param component the component
     * @throws Fabric3Exception if an error occurs selecting a binding
     */
    private void selectBindings(LogicalComponent<?> component) throws Fabric3Exception {
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
     * @throws Fabric3Exception if an error occurs selecting a binding
     */
    private void selectBinding(LogicalChannel channel) throws Fabric3Exception {
        if (channel.isBound() || channel.getDefinition().isLocal()) {
            return;
        }
        for (BindingProvider provider : providers) {
            BindingMatchResult result = provider.canBind(channel);
            if (result.isMatch()) {
                provider.bind(channel);
                if (channel.getBindings().isEmpty()) {
                    QName type = result.getType();
                    throw new Fabric3Exception("Binding provider error. Provider did not set a binding for the channel: " + type);
                }
                return;
            }
        }
        URI uri = channel.getUri();
        throw new Fabric3Exception("No SCA binding provider suitable for channel " + uri);
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

