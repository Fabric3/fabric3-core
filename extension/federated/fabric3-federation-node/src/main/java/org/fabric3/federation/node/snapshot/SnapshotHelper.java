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
 */
package org.fabric3.federation.node.snapshot;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.remote.RemoteImplementation;
import org.fabric3.spi.model.type.remote.RemoteServiceContract;

/**
 * Creates a snapshots of the logical domain model.
 */
public class SnapshotHelper {

    /**
     * Creates a snapshot of the entire logical domain.
     *
     * @param domain the domain composite
     * @param state  the state to set the snapshot artifacts to
     * @return the snapshot
     */
    public static LogicalCompositeComponent snapshot(LogicalCompositeComponent domain, LogicalState state) {
        return snapshot(domain, null, state);
    }

    /**
     * Creates a snapshot of the contribution artifacts (component and channels) in the logical domain.
     *
     * @param domain the domain composite
     * @param uri    the contribution URI
     * @param state  the state to set the snapshot artifacts to
     * @return the snapshot
     */
    public static LogicalCompositeComponent snapshot(LogicalCompositeComponent domain, URI uri, LogicalState state) {
        String domainName = domain.getDefinition().getName();
        Composite typeCopy = new Composite(null);
        CompositeImplementation implementationCopy = new CompositeImplementation();
        implementationCopy.setComponentType(typeCopy);
        Component<CompositeImplementation> compositeCopy = new Component<>(domainName, implementationCopy);
        LogicalCompositeComponent domainCopy = new LogicalCompositeComponent(domain.getUri(), compositeCopy, null);
        for (LogicalComponent<?> component : domain.getComponents()) {
            if (uri == null || uri.equals(component.getDefinition().getContributionUri())) {
                if (!isReplicable(component)) {
                    continue;
                }
                LogicalComponent<?> componentCopy = snapshot(component, state, domainCopy);
                domainCopy.addComponent(componentCopy);
            }
        }
        for (LogicalChannel channel : domain.getChannels()) {
            if (channel.getBindings().isEmpty()) {
                continue;
            }
            LogicalChannel channelCopy = snapshot(channel, typeCopy, state, domainCopy);
            domainCopy.addChannel(channelCopy);
        }
        return domainCopy;
    }

    static LogicalChannel snapshot(LogicalChannel channel, Composite composite, LogicalState state, LogicalCompositeComponent domain) {
        Channel definition = channel.getDefinition();
        String name = definition.getName();
        String type = definition.getType();
        boolean local = definition.isLocal();
        Channel definitionCopy = new Channel(name, type, local);
        definitionCopy.setParent(composite);
        definitionCopy.setLocal(definition.isLocal());
        LogicalChannel channelCopy = new LogicalChannel(channel.getUri(), definitionCopy, domain);
        channelCopy.getBindings().addAll(channel.getBindings());
        channelCopy.setDeployable(channel.getDeployable());
        channelCopy.setZone(channel.getZone());
        channelCopy.setState(state);
        return channelCopy;
    }

    static LogicalComponent<?> snapshot(LogicalComponent<?> component, LogicalState state, LogicalCompositeComponent parent) {
        Component<? extends Implementation<?>> definition = component.getDefinition();
        String name = definition.getName();

        RemoteImplementation remoteImplementation = new RemoteImplementation();
        ComponentType typeCopy = new ComponentType();
        ComponentType type = definition.getComponentType();
        for (Service<ComponentType> abstractDefinition : type.getServices().values()) {
            Service<ComponentType> serviceCopy = snapshot(abstractDefinition);
            typeCopy.add(serviceCopy);
        }
        remoteImplementation.setComponentType(typeCopy);

        Component<RemoteImplementation> definitionCopy = new Component<>(name, remoteImplementation);
        Composite composite = (Composite) parent.getDefinition().getComponentType();
        definitionCopy.setParent(composite);
        definitionCopy.setContributionUri(definition.getContributionUri());

        URI uri = component.getUri();
        LogicalComponent<RemoteImplementation> componentCopy = new LogicalComponent<>(uri, definitionCopy, parent);
        componentCopy.setDeployable(component.getDeployable());
        componentCopy.setZone(component.getZone());
        componentCopy.setState(state);

        for (LogicalService service : component.getServices()) {
            LogicalService serviceCopy = snapshot(service, componentCopy);
            componentCopy.addService(serviceCopy);
        }

        return componentCopy;
    }

    static LogicalService snapshot(LogicalService service, LogicalComponent<RemoteImplementation> parent) {
        Service<ComponentType> abstractDefinition = service.getDefinition();
        Service<ComponentType> serviceDefinitionCopy = snapshot(abstractDefinition);
        URI serviceUri = service.getUri();
        LogicalService serviceCopy = new LogicalService(serviceUri, serviceDefinitionCopy, parent);
        serviceCopy.getBindings().addAll(service.getBindings());
        return serviceCopy;
    }

    static <P extends ModelObject> Service<P> snapshot(Service<P> abstractDefinition) {
        ServiceContract contract = abstractDefinition.getServiceContract();
        String serviceName = abstractDefinition.getName();
        ServiceContract contractCopy = snapshot(contract);
        return new Service<>(serviceName, contractCopy);
    }

    static RemoteServiceContract snapshot(ServiceContract contract) {
        String interfaceName = contract.getQualifiedInterfaceName();
        List<String> superTypes = new ArrayList<>();
        if (contract instanceof JavaServiceContract) {
            JavaServiceContract javaContract = (JavaServiceContract) contract;
            superTypes.addAll(javaContract.getInterfaces());
        }
        RemoteServiceContract contractCopy = new RemoteServiceContract(interfaceName, superTypes);
        ServiceContract callbackContract = contract.getCallbackContract();
        if (callbackContract != null) {
            ServiceContract callbackContractCopy = snapshot(callbackContract);
            contractCopy.setCallbackContract(callbackContractCopy);
        }
        return contractCopy;
    }

    /**
     * True if the component should be replicated. Only components with at least one remotable service or bound service are replicable.
     *
     * @param component the component
     * @return true if the component is replicable
     */
    private static boolean isReplicable(LogicalComponent<?> component) {
        for (LogicalService service : component.getServices()) {
            ServiceContract contract = service.getLeafService().getServiceContract();
            if (contract.isRemotable() || !service.getBindings().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private SnapshotHelper() {
    }

}
