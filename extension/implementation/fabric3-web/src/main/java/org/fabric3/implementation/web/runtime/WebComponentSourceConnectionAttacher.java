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
package org.fabric3.implementation.web.runtime;

import java.net.URI;

import org.fabric3.implementation.web.provision.WebComponentConnectionSourceDefinition;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class WebComponentSourceConnectionAttacher implements SourceConnectionAttacher<WebComponentConnectionSourceDefinition> {
    private ComponentManager manager;

    public WebComponentSourceConnectionAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(WebComponentConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        URI sourceUri = UriHelper.getDefragmentedName(source.getUri());
        String producerName = source.getUri().getFragment();
        WebComponent component = (WebComponent) manager.getComponent(sourceUri);
        component.connect(producerName, connection);

    }

    public void detach(WebComponentConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ContainerException {

    }

}
