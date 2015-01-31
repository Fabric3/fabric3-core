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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.proxy;

import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyService;
import org.fabric3.implementation.pojo.spi.proxy.ChannelProxyServiceExtension;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelProxyServiceImpl implements ChannelProxyService {
    private ChannelProxyServiceExtension extension;

    @Reference(required = false)
    public void setExtensions(List<ChannelProxyServiceExtension> extensions) {
        if (extensions.isEmpty()) {
            return;
        }
        if (extensions.size() == 1) {
            extension = extensions.get(0);
        } else {
            if (extension != null && !extension.isDefault()) {
                return;
            }
            for (ChannelProxyServiceExtension entry : extensions) {
                if (!entry.isDefault()) {
                    extension = entry;
                    return;
                }
            }

        }
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, ChannelConnection connection) throws ContainerException {
        if (extension == null) {
            throw new ContainerException("Channel proxy service extension not installed");
        }
        return extension.createObjectFactory(interfaze, connection);
    }
}
