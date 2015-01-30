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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * An artifact which can be bound to a remote transport.
 */
public abstract class LogicalBindable extends LogicalInvocable {
    private static final long serialVersionUID = 570403036597601956L;
    private List<LogicalBinding<?>> bindings;
    private List<LogicalBinding<?>> callbackBindings;

    /**
     * Initializes the URI and parent for the service or the reference.
     *
     * @param uri      URI of the service or the reference.
     * @param contract the service contract
     * @param parent   Parent of the service or the reference.
     */
    protected LogicalBindable(URI uri, ServiceContract contract, LogicalComponent<?> parent) {
        super(uri, contract, parent);
        bindings = new ArrayList<>();
        callbackBindings = new ArrayList<>();
    }

    /**
     * Returns all the bindings on the service or the reference.
     *
     * @return The bindings available on the service or the reference.
     */
    public List<LogicalBinding<?>> getBindings() {
        return bindings;
    }

    /**
     * Convenience method.
     *
     * @return true if the channel is configured with a binding
     */
    public boolean isBound() {
        return !getBindings().isEmpty();
    }

    /**
     * Returns all the callback bindings on the service or the reference.
     *
     * @return The bindings available on the service or the reference.
     */
    public List<LogicalBinding<?>> getCallbackBindings() {
        return callbackBindings;
    }

    /**
     * Adds a binding to the service or the reference.
     *
     * @param binding Binding to be added to the service or the reference.
     */
    public void addBinding(LogicalBinding<?> binding) {
        bindings.add(binding);
    }

    /**
     * Adds a callback binding to the service or the reference.
     *
     * @param binding Binding to be added to the service or the reference.
     */
    public void addCallbackBinding(LogicalBinding<?> binding) {
        binding.setCallback(true);
        callbackBindings.add(binding);
    }

}
