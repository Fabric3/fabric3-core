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

import org.fabric3.binding.ws.metro.provision.MetroWireTarget;
import org.fabric3.binding.ws.metro.runtime.core.SOAPMessageHandlerAdapter;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalBindingHandler;
import org.fabric3.spi.model.physical.PhysicalWireTarget;

/**
 * Base {@link TargetWireAttacher} functionality for web services.
 */
public abstract class AbstractMetroTargetWireAttacher<T extends PhysicalWireTarget> implements TargetWireAttacher<T> {
    private BindingHandlerRegistry handlerRegistry;

    public AbstractMetroTargetWireAttacher(BindingHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    protected List<Handler> createHandlers(MetroWireTarget target) {
        if (target.getHandlers().isEmpty() && !target.isBidirectional() && !target.isCallback()) {
            return null;
        }
        List<Handler> handlers = new ArrayList<>();

        for (PhysicalBindingHandler physicalHandler : target.getHandlers()) {
            BindingHandler<SOAPMessage> handler = handlerRegistry.createHandler(SOAPMessage.class, physicalHandler);
            SOAPMessageHandlerAdapter adaptor = new SOAPMessageHandlerAdapter(handler);
            handlers.add(adaptor);
        }
        return handlers;
    }

}