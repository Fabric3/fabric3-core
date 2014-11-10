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
package org.fabric3.binding.web.runtime.service;

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.web.provision.WebWireTargetDefinition;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;

/**
 * Attaches a callback proxy to a websocket.
 */
@EagerInit
public class WebTargetWireAttacher implements TargetWireAttacher<WebWireTargetDefinition> {
    private BroadcasterManager broadcasterManager;

    public WebTargetWireAttacher(@Reference BroadcasterManager broadcasterManager) {
        this.broadcasterManager = broadcasterManager;
    }

    public void attach(PhysicalWireSourceDefinition source, WebWireTargetDefinition target, Wire wire) throws ContainerException {
        WebCallbackInterceptor interceptor = new WebCallbackInterceptor(broadcasterManager);
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalWireSourceDefinition source, WebWireTargetDefinition target) throws ContainerException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(WebWireTargetDefinition target) throws ContainerException {
        throw new UnsupportedOperationException();
    }
}
