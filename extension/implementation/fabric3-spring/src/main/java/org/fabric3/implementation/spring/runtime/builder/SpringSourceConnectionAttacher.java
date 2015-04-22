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
package org.fabric3.implementation.spring.runtime.builder;

import java.net.URI;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.spring.provision.SpringConnectionSource;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.container.builder.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches a {@link ChannelConnection} from a Spring component producer.
 */
@EagerInit
public class SpringSourceConnectionAttacher implements SourceConnectionAttacher<SpringConnectionSource> {
    private ComponentManager manager;
    private ChannelProxyService proxyService;

    public SpringSourceConnectionAttacher(@Reference ComponentManager manager, @Reference ChannelProxyService proxyService) {
        this.manager = manager;
        this.proxyService = proxyService;
    }

    public void attach(SpringConnectionSource source, PhysicalConnectionTarget target, ChannelConnection connection)
            throws Fabric3Exception {
        URI sourceUri = source.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceUri);
        SpringComponent component = (SpringComponent) manager.getComponent(sourceName);
        if (component == null) {
            throw new Fabric3Exception("Source component not found: " + sourceName);
        }
        Class<?> type = source.getServiceInterface();

        Supplier<?> supplier;
        if (source.isDirectConnection()) {
            supplier = connection.getDirectConnection().get();
        } else {
            supplier = proxyService.createSupplier(type, connection);
        }
        component.attach(source.getProducerName(), type, supplier);
    }

    public void detach(SpringConnectionSource source, PhysicalConnectionTarget target) throws Fabric3Exception {
        URI sourceName = UriHelper.getDefragmentedName(source.getUri());
        SpringComponent component = (SpringComponent) manager.getComponent(sourceName);
        component.detach(source.getProducerName());
    }

}