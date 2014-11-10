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
package org.fabric3.binding.ws.metro.runtime.wire;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.runtime.core.CallbackTargetHandler;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.ReferenceCallbackAddressHandler;
import org.fabric3.binding.ws.metro.runtime.core.SOAPMessageHandlerAdapter;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Base {@link TargetWireAttacher} functionality for web services.
 */
public abstract class AbstractMetroTargetWireAttacher<T extends PhysicalWireTargetDefinition> implements TargetWireAttacher<T> {
    private BindingHandlerRegistry handlerRegistry;
    private EndpointService endpointService;

    public AbstractMetroTargetWireAttacher(BindingHandlerRegistry handlerRegistry, EndpointService endpointService) {
        this.handlerRegistry = handlerRegistry;
        this.endpointService = endpointService;
    }

    protected List<Handler> createHandlers(MetroWireTargetDefinition target) {
        if (target.getHandlers().isEmpty() && !target.isBidirectional() && !target.isCallback()) {
            return null;
        }
        List<Handler> handlers = new ArrayList<>();

        if (target.isBidirectional()) {
            ReferenceCallbackAddressHandler callbackHandler = new ReferenceCallbackAddressHandler(target.getCallbackUri(), endpointService);
            handlers.add(callbackHandler);
        }  else if (target.isCallback()) {
            CallbackTargetHandler handler = new CallbackTargetHandler();
            handlers.add(handler);
        }

        for (PhysicalBindingHandlerDefinition handlerDefinition : target.getHandlers()) {
            BindingHandler<SOAPMessage> handler = handlerRegistry.createHandler(SOAPMessage.class, handlerDefinition);
            SOAPMessageHandlerAdapter adaptor = new SOAPMessageHandlerAdapter(handler);
            handlers.add(adaptor);
        }
        return handlers;
    }

}