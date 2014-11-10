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
package org.fabric3.binding.rs.runtime.container;

import javax.inject.Singleton;
import java.util.Set;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.glassfish.jersey.server.spi.internal.ResourceMethodInvocationHandlerProvider;

/**
 * Initializes the HK2 ServiceLocator context with Fabric3 extensions.
 */
public class Fabric3ComponentProvider implements ComponentProvider {

    public void initialize(ServiceLocator locator) {
        // bind the method handler provider
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration dc = dcs.createDynamicConfiguration();
        Binder binder = new AbstractBinder() {
            protected void configure() {
                bind(F3ResourceMethodInvocationHandlerProvider.class).to(ResourceMethodInvocationHandlerProvider.class).in(Singleton.class);
            }
        };
        locator.inject(binder);
        binder.bind(dc);

        dc.commit();
    }

    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        return false;
    }

    public void done() {
    }
}
