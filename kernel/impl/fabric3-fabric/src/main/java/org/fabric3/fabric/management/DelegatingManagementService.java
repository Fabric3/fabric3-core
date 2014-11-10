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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.management;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementExtension;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * An implementation that delegates exporting managed resources to a collection of {@link ManagementExtension}s. This implementation caches export
 * requests so that management extensions which come online after an export request has been made will receive that request.
 */
public class DelegatingManagementService implements ManagementService {
    private Map<String, ManagementExtension> extensions = new HashMap<>();
    private List<ComponentHolder> componentHolders = new ArrayList<>();
    private List<InstanceHolder> instanceHolders = new ArrayList<>();

    /**
     * Setter to allow for reinjection of new management extensions.
     *
     * @param injected the reinjected management extensions
     * @throws ManagementException if an error is encountered registering previous export requests
     */
    @Reference(required = false)
    public void setExtensions(List<ManagementExtension> injected) throws ManagementException {
        extensions.clear();
        for (ManagementExtension extension : injected) {
            extensions.put(extension.getType(), extension);
        }
        exportComponents();
        exportInstances();
    }

    public void export(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) throws ManagementException {
        ComponentHolder holder = new ComponentHolder(componentUri, info, objectFactory, classLoader);
        for (Map.Entry<String, ManagementExtension> entry : extensions.entrySet()) {
            String type = entry.getKey();
            ManagementExtension extension = entry.getValue();
            extension.export(componentUri, info, objectFactory, classLoader);
            holder.registered.add(type);
        }
        componentHolders.add(holder);
    }

    public void export(String name, String group, String description, Object instance) throws ManagementException {
        InstanceHolder holder = new InstanceHolder(name, group, description, instance);
        for (Map.Entry<String, ManagementExtension> entry : extensions.entrySet()) {
            String type = entry.getKey();
            ManagementExtension extension = entry.getValue();
            extension.export(name, group, description, instance);
            holder.registered.add(type);
        }
        instanceHolders.add(holder);
    }

    public void remove(URI componentUri, ManagementInfo info) throws ManagementException {
        for (Iterator<ComponentHolder> iterator = componentHolders.iterator(); iterator.hasNext();) {
            ComponentHolder holder = iterator.next();
            if (holder.componentUri.equals(componentUri)) {
                for (String type : holder.registered) {
                    ManagementExtension extension = extensions.get(type);
                    if (extension != null) {
                        extension.remove(componentUri, info);
                    }
                }
                iterator.remove();
                return;
            }
        }
    }

    public void remove(String name, String group) throws ManagementException {
        for (Iterator<InstanceHolder> iterator = instanceHolders.iterator(); iterator.hasNext();) {
            InstanceHolder holder = iterator.next();
            if (holder.name.equals(name) && holder.group.equals(group)) {
                for (String type : holder.registered) {
                    ManagementExtension extension = extensions.get(type);
                    if (extension != null) {
                        extension.remove(name, group);
                    }
                }
                iterator.remove();
                return;
            }
        }
    }

    private void exportComponents() throws ManagementException {
        for (Map.Entry<String, ManagementExtension> entry : extensions.entrySet()) {
            String type = entry.getKey();
            ManagementExtension extension = entry.getValue();
            for (ComponentHolder holder : componentHolders) {
                if (!holder.registered.contains(type)) {
                    extension.export(holder.componentUri, holder.info, holder.objectFactory, holder.classLoader);
                    holder.registered.add(type);
                }
            }
        }
    }

    private void exportInstances() throws ManagementException {
        for (Map.Entry<String, ManagementExtension> entry : extensions.entrySet()) {
            String type = entry.getKey();
            ManagementExtension extension = entry.getValue();
            for (InstanceHolder holder : instanceHolders) {
                if (!holder.registered.contains(type)) {
                    extension.export(holder.name, holder.group, holder.description, holder.instance);
                    holder.registered.add(type);
                }
            }
        }
    }

    private class ComponentHolder {
        private URI componentUri;
        private ManagementInfo info;
        private ObjectFactory objectFactory;
        private ClassLoader classLoader;
        private List<String> registered = new ArrayList<>();

        public ComponentHolder(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) {
            this.componentUri = componentUri;
            this.info = info;
            this.objectFactory = objectFactory;
            this.classLoader = classLoader;
        }
    }

    private class InstanceHolder {
        private String name;
        private String group;
        private String description;
        private Object instance;
        private List<String> registered = new ArrayList<>();

        private InstanceHolder(String name, String group, String description, Object instance) {
            this.name = name;
            this.group = group;
            this.description = description;
            this.instance = instance;
        }
    }
}
