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
package org.fabric3.fabric.container.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistryCallback;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;

/**
 *
 */
public class BindingHandlerRegistryImpl implements BindingHandlerRegistry {
    private ComponentManager componentManager;

    private Map<QName, BindingHandlerRegistryCallback<?>> callbacks = new HashMap<>();
    private Map<QName, List<BindingHandler<?>>> handlers = new HashMap<>();

    public BindingHandlerRegistryImpl(@Reference ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public <T> BindingHandler<T> createHandler(Class<T> type, PhysicalBindingHandlerDefinition definition) {
        return new BindingHandlerLazyLoadDecorator<>(definition.getHandlerUri(), componentManager);
    }

    @SuppressWarnings({"unchecked"})
    public synchronized void register(BindingHandlerRegistryCallback callback) {
        QName binding = callback.getType();
        if (callbacks.containsKey(binding)) {
            throw new IllegalStateException("Callback already registered for " + binding);
        }
        callbacks.put(binding, callback);
        List<BindingHandler<?>> list = handlers.get(binding);
        if (list != null) {
            // notify the callback of existing handlers
            callback.update(Collections.unmodifiableList(list));
        }
    }

    public synchronized void unregister(BindingHandlerRegistryCallback<?> callback) {
        QName binding = callback.getType();
        if (callbacks.remove(binding) == null) {
            throw new IllegalStateException("Callback not registered for " + binding);
        }
    }

    @SuppressWarnings({"unchecked"})
    public synchronized void register(BindingHandler<?> handler) {
        QName binding = handler.getType();
        List<BindingHandler<?>> list = handlers.get(binding);
        if (list == null) {
            list = new ArrayList<>();
            handlers.put(binding, list);
        }
        list.add(handler);
        BindingHandlerRegistryCallback callback = callbacks.get(binding);
        if (callback != null) {
            callback.update(Collections.unmodifiableList(list));
        }
    }

    @SuppressWarnings({"unchecked"})
    public synchronized void unregister(BindingHandler<?> handler) {
        QName binding = handler.getType();
        List<BindingHandler<?>> list = handlers.get(binding);
        if (list == null || !list.remove(handler)) {
            throw new IllegalStateException("Handler not registered for binding " + binding);
        }
        if (list.isEmpty()) {
            handlers.remove(binding);
        }
        BindingHandlerRegistryCallback callback = callbacks.get(binding);
        if (callback != null) {
            callback.update(Collections.unmodifiableList(list));
        }
    }

}
