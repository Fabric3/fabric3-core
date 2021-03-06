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

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyServiceExtension;
import org.fabric3.spi.container.wire.Wire;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class WireProxyServiceImpl implements WireProxyService {
    private WireProxyServiceExtension extension;

    @Reference(required = false)
    public void setExtensions(List<WireProxyServiceExtension> extensions) {
        if (extensions.isEmpty()) {
            return;
        }
        if (extensions.size() == 1) {
            extension = extensions.get(0);
        } else {
            if (extension != null && !extension.isDefault()) {
                return;
            }
            for (WireProxyServiceExtension entry : extensions) {
                if (!entry.isDefault()) {
                    extension = entry;
                    return;
                }
            }

        }
    }

    public <T> Supplier<T> createSupplier(Class<T> interfaze, Wire wire, String callbackUri) {
        checkExtension();
        return extension.createSupplier(interfaze, wire, callbackUri);
    }

    public <T> Supplier<T> createCallbackSupplier(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire) {
        checkExtension();
        return extension.createCallbackSupplier(interfaze, multiThreaded, callbackUri, wire);
    }

    public <T> Supplier<?> updateCallbackSupplier(Supplier<?> supplier, Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire) {
        checkExtension();
        return extension.updateCallbackSupplier(supplier, interfaze, multiThreaded, callbackUri, wire);
    }

    private void checkExtension() {
        if (extension == null) {
            throw new Fabric3Exception("Channel proxy service extension not installed");
        }
    }

}
