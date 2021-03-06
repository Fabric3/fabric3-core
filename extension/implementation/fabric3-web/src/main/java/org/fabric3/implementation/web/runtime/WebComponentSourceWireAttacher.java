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
package org.fabric3.implementation.web.runtime;

import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.implementation.web.provision.WebWireSource;
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Source WireAttacher for web components.
 */
@EagerInit
public class WebComponentSourceWireAttacher implements SourceWireAttacher<WebWireSource> {
    private ComponentManager manager;

    public WebComponentSourceWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(WebWireSource source, PhysicalWireTarget target, Wire wire) {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        component.attachWire(referenceName, wire);
    }

    public void detach(WebWireSource source, PhysicalWireTarget target) {
        // TODO implement
    }

    public void detachSupplier(WebWireSource source, PhysicalWireTarget target) {
        // no-op
    }

    public void attachSupplier(WebWireSource source, Supplier<?> supplier, PhysicalWireTarget target) {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String referenceName = source.getUri().getFragment();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        component.attach(referenceName, supplier);
    }
}