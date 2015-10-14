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
package org.fabric3.binding.zeromq.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.binding.zeromq.annotation.ZeroMQ;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.AbstractBindingPostProcessor;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Introspects ZeroMQ binding information in a component implementation.
 */
@EagerInit
public class ZeroMQPostProcessor extends AbstractBindingPostProcessor<ZeroMQ> {

    public ZeroMQPostProcessor() {
        super(ZeroMQ.class);
    }

    protected Binding processReference(ZeroMQ annotation, Reference reference, Class<?> implClass, IntrospectionContext context) {
        if(!isActiveForEnvironment(annotation.environments())) {
            return null;
        }

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        String bindingName = "ZMQ" + reference.getName();
        ZeroMQBinding binding = new ZeroMQBinding(bindingName, metadata);

        parseTarget(annotation, binding, implClass, context);
        parseAddresses(annotation, metadata, implClass, context);

        processMetadata(annotation, metadata);
        return binding;
    }

    protected Binding processService(ZeroMQ annotation,
                                     Service<ComponentType> boundService,
                                     InjectingComponentType componentType,
                                     Class<?> implClass,
                                     IntrospectionContext context) {
        if(!isActiveForEnvironment(annotation.environments())) {
            return null;
        }

        Class<?> serviceInterface = ((JavaServiceContract) boundService.getServiceContract()).getInterfaceClass();

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        String bindingName = "ZMQ" + serviceInterface.getSimpleName();
        ZeroMQBinding binding = new ZeroMQBinding(bindingName, metadata);
        int port = annotation.port();
        if (port > 0) {
            SocketAddressDefinition address = new SocketAddressDefinition("localhost", port);
            metadata.setSocketAddresses(Collections.singletonList(address));
        } else {
            parseAddresses(annotation, metadata, implClass, context);
        }
        processMetadata(annotation, metadata);
        return binding;
    }

    protected Binding processServiceCallback(ZeroMQ annotation,
                                             Service<ComponentType> service,
                                             InjectingComponentType componentType,
                                             Class<?> implClass,
                                             IntrospectionContext context) {
        return null; // not needed
    }

    protected Binding processReferenceCallback(ZeroMQ annotation, Reference reference, Class<?> implClass, IntrospectionContext context) {
        return null; // not needed
    }

    private void processMetadata(ZeroMQ annotation, ZeroMQMetadata metadata) {
        metadata.setTimeout(annotation.timeout());
        metadata.setHighWater(annotation.highWater());
        metadata.setMulticastRate(annotation.multicastRate());
        metadata.setReceiveBuffer(annotation.receiveBuffer());
        metadata.setMulticastRecovery(annotation.multicastRecovery());
        metadata.setSendBuffer(annotation.sendBuffer());
        metadata.setWireFormat(annotation.wireFormat());
    }

    private void parseAddresses(ZeroMQ annotation, ZeroMQMetadata metadata, Class<?> implClass, IntrospectionContext context) {
        String addresses = annotation.addresses();
        if (addresses.length() == 0) {
            return;
        }
        List<SocketAddressDefinition> addressDefinitions = new ArrayList<>();
        String[] addressStrings = addresses.split("\\s+");
        for (String entry : addressStrings) {
            String[] tokens = entry.split(":");
            if (tokens.length != 2) {
                context.addError(new InvalidAnnotation("Invalid address specified on ZeroMQ binding: " + entry, null, annotation, implClass));
            } else {
                try {
                    String host = tokens[0];
                    int port = Integer.parseInt(tokens[1]);
                    addressDefinitions.add(new SocketAddressDefinition(host, port));
                } catch (NumberFormatException e) {
                    context.addError(new InvalidAnnotation("Invalid port specified on ZeroMQ binding: " + e.getMessage(), null, annotation, implClass));
                }
            }
        }
        metadata.setSocketAddresses(addressDefinitions);
    }

    private void parseTarget(ZeroMQ annotation, ZeroMQBinding binding, Class<?> implClass, IntrospectionContext context) {
        String target = annotation.target();
        try {
            URI targetUri = new URI(target);
            binding.setTargetUri(targetUri);
        } catch (URISyntaxException e) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid target URI specified on ZeroMQ annotation: " + target, null, annotation, implClass, e);
            context.addError(error);
        }
    }

}
