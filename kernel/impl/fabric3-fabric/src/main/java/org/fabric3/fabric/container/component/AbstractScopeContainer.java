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
package org.fabric3.fabric.container.component;

import org.fabric3.api.model.type.component.Scope;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.component.ScopedComponent;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implements functionality common to scope containers.
 */
public abstract class AbstractScopeContainer implements ScopeContainer {
    private Scope scope;
    protected ScopeContainerMonitor monitor;
    private ScopeRegistry scopeRegistry;

    public AbstractScopeContainer(Scope scope, ScopeContainerMonitor monitor) {
        this.scope = scope;
        this.monitor = monitor;
    }

    @Reference
    public void setScopeRegistry(ScopeRegistry scopeRegistry) {
        this.scopeRegistry = scopeRegistry;
    }

    public synchronized void start() {
        if (scopeRegistry != null) {
            scopeRegistry.register(this);
        }
    }

    public synchronized void stop() {
        if (scopeRegistry != null) {
            scopeRegistry.unregister(this);
        }
    }

    public Scope getScope() {
        return scope;
    }

    public void register(ScopedComponent component) {
    }

    public void unregister(ScopedComponent component) {
    }

    public String toString() {
        return "In state [" + super.toString() + ']';
    }


}
