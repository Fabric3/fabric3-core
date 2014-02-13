/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
