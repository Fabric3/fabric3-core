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
package org.fabric3.container.web.jetty;

import java.util.List;
import java.util.Map;

import org.eclipse.jetty.util.Decorator;
import org.fabric3.spi.container.injection.Injector;

/**
 * Injects a servlet or filter with reference proxies, properties, and the component context.
 */
public class InjectingDecorator implements Decorator {
    private Map<String, List<Injector<?>>> injectorMappings;

    public InjectingDecorator(Map<String, List<Injector<?>>> injectorMappings) {
        this.injectorMappings = injectorMappings;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T decorate(T instance) {
        List<Injector<?>> injectors = injectorMappings.get(instance.getClass().getName());
        if (injectors != null) {
            for (Injector injector : injectors) {
                injector.inject(instance);
            }
        }
        return instance;
    }

    public void destroy(Object o) {

    }
}
