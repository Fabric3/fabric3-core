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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;

/**
 * Default implementation of the component manager.
 */
public class ComponentManagerImpl implements ComponentManager {
    private Map<URI, Component> components;

    public ComponentManagerImpl() {
        components = new ConcurrentHashMap<>();
    }

    public synchronized void register(Component component) throws ContainerException {
        URI uri = component.getUri();
        if (components.containsKey(uri)) {
            throw new ContainerException("A component is already registered for: " + uri.toString());
        }
        components.put(uri, component);
    }

    public synchronized Component unregister(URI uri) throws ContainerException {
        return components.remove(uri);
    }

    public Component getComponent(URI name) {
        return components.get(name);
    }

    public List<Component> getComponents() {
        return new ArrayList<>(components.values());
    }

    public List<Component> getComponentsInHierarchy(URI uri) {
        String stringified = uri.toString();
        List<Component> hierarchy = new ArrayList<>();
        for (Component component : components.values()) {
            URI componentUri = component.getUri();
            if (componentUri.toString().startsWith(stringified)) {
                hierarchy.add(component);
            }
        }
        return hierarchy;
    }

    public List<Component> getDeployedComponents(QName deployable) {
        List<Component> deployed = new ArrayList<>();
        for (Component component : components.values()) {
            if (deployable.equals(component.getDeployable())) {
                deployed.add(component);
            }
        }
        return deployed;
    }
}
