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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.spring.provision.SpringConnectionTarget;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.implementation.spring.runtime.component.SpringEventStreamHandler;
import org.fabric3.spi.container.builder.component.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches a {@link ChannelConnection} from a Spring component consumer.
 */
@EagerInit
public class SpringTargetConnectionAttacher implements TargetConnectionAttacher<SpringConnectionTarget> {
    private ComponentManager manager;

    public SpringTargetConnectionAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(PhysicalConnectionSource source, SpringConnectionTarget target, ChannelConnection connection) {
        URI targetUri = target.getUri();
        SpringComponent component = (SpringComponent) manager.getComponent(targetUri);
        if (component == null) {
            throw new Fabric3Exception("Target component not found: " + targetUri);
        }
        String beanName = target.getBeanName();
        JavaType type = target.getType();
        String consumerName = target.getMethodName();
        SpringEventStreamHandler handler = new SpringEventStreamHandler(beanName, consumerName, type, component);
        EventStream stream = connection.getEventStream();
        stream.addHandler(handler);
    }

    public void detach(PhysicalConnectionSource source, SpringConnectionTarget target) {
        // no-op
    }

}