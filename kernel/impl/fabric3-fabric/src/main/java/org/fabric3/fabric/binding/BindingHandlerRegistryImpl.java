/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.binding.handler.BindingHandlerDefinition;
import org.fabric3.spi.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.binding.handler.BindingHandlerRegistryCallback;
import org.fabric3.spi.cm.ComponentManager;

/**
 * @version $Rev$ $Date$
 */
public class BindingHandlerRegistryImpl implements BindingHandlerRegistry {
    private Map<QName, BindingHandlerRegistryCallback<?>> callbacks = new HashMap<QName, BindingHandlerRegistryCallback<?>>();
    private Map<QName, List<BindingHandler<?>>> handlers = new HashMap<QName, List<BindingHandler<?>>>();

    private Map<BinderKey, List<BindingHandlerDefinition>> handlerDefinitions = new HashMap<BinderKey, List<BindingHandlerDefinition>>();

    private ComponentManager componentManager;

    public BindingHandlerRegistryImpl(@Reference ComponentManager componentManager) {
        this.componentManager = componentManager;
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
            list = new ArrayList<BindingHandler<?>>();
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

    public void register(QName bindingQname, String path, BindingHandlerDefinition definition) {
        BinderKey binderKey = new BinderKey(bindingQname, path);
        List<BindingHandlerDefinition> definitions = handlerDefinitions.get(binderKey);
        if (definitions == null) {
            definitions = new ArrayList<BindingHandlerDefinition>();
            handlerDefinitions.put(binderKey, definitions);
        }
        definitions.add(definition);
    }

    public List<BindingHandler<?>> loadBindingHandlers(QName bindingQname, String servicePath) {
        BinderKey key = new BinderKey(bindingQname, servicePath);
        List<BindingHandlerDefinition> definitions = handlerDefinitions.get(key);
        List<BindingHandler<?>> result = new ArrayList<BindingHandler<?>>();
        if (definitions != null) {
            for (BindingHandlerDefinition bhd : definitions) {
                result.add(createHandler(bhd));
            }
        }
        return result;
    }

    public void unregister(QName bindingQname, String path) {
        handlerDefinitions.remove(new BinderKey(bindingQname, path));
    }

    private BindingHandler<?> createHandler(BindingHandlerDefinition bh) {
        try {
            return new BindingHandlerLazyLoadDecorator<Object>(bh.getTarget(), componentManager);
        } catch (Exception cause) {
            throw new RuntimeException(cause);
        }
    }


    private static class BinderKey {

        private QName type;
        private String path;

        public BinderKey(QName bindingQname, String p) {
            type = bindingQname;
            path = p;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BinderKey other = (BinderKey) obj;
            if (path == null) {
                if (other.path != null)
                    return false;
            } else if (!path.equals(other.path))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }

    }
}
