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
package org.fabric3.api.host.runtime;

/**
 * Records an extension component provided by the host environment that must be registered with the runtime domain.
 */
public class ComponentRegistration {
    private String name;
    private Class<?> service;
    private Object instance;
    private boolean introspect;

    /**
     * Constructor.
     *
     * @param name       the component name
     * @param service    the component service contract
     * @param instance   the component instance
     * @param introspect true if the component should be introspected and a component type generated
     * @param <S>        the service contract type
     * @param <I>        the component instance
     */
    public <S, I extends S> ComponentRegistration(String name, Class<S> service, I instance, boolean introspect) {
        this.name = name;
        this.service = service;
        this.instance = instance;
        this.introspect = introspect;
    }

    public String getName() {
        return name;
    }

    public Class<?> getService() {
        return service;
    }

    public Object getInstance() {
        return instance;
    }

    public boolean isIntrospect() {
        return introspect;
    }
}
