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
package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.ext.Provider;
import java.net.URI;

import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.component.ScopedComponent;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 *
 */
@Provider
public class AbstractProxyProvider<T> {

    private URI filterUri;
    private ComponentManager componentManager;

    private volatile ScopedComponent delegate;

    public AbstractProxyProvider() {
    }

    public void init(URI filterUri, ComponentManager componentManager) {
        this.filterUri = filterUri;
        this.componentManager = componentManager;
    }

    @SuppressWarnings("unchecked")
    public T getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                Component component = componentManager.getComponent(filterUri);
                if (component == null) {
                    throw new ServiceUnavailableException("Provider component not found: " + filterUri);
                }
                if (!(component instanceof ScopedComponent)) {
                    throw new ServiceRuntimeException("Provider component must be a scoped component type: " + filterUri);
                }
                delegate = (ScopedComponent) component;
            }
        }
        try {
            return ((T) delegate.getInstance());
        } catch (InstanceLifecycleException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
