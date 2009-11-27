/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.cm;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.cm.RegistrationException;
import org.fabric3.spi.component.Component;

/**
 * Default implementation of the component manager
 *
 * @version $Rev$ $Date$
 */
public class ComponentManagerImpl implements ComponentManager {
    private final Map<URI, Component> components;

    public ComponentManagerImpl() {
        components = new ConcurrentHashMap<URI, Component>();
    }

    public synchronized void register(Component component) throws RegistrationException {
        URI uri = component.getUri();

        assert uri != null;
        assert !uri.toString().endsWith("/");
        if (components.containsKey(uri)) {
            throw new DuplicateComponentException("A component is already registered for: " + uri.toString());
        }
        components.put(uri, component);
    }

    public synchronized void unregister(Component component) throws RegistrationException {
        URI uri = component.getUri();
        components.remove(uri);
    }

    public Component getComponent(URI name) {
        return components.get(name);
    }

    public List<Component> getComponents() {
        return new ArrayList<Component>(components.values());
    }

    public List<URI> getComponentsInHierarchy(URI uri) {
        String strigified = uri.toString();
        List<URI> uris = new ArrayList<URI>();
        for (Component component : components.values()) {
            URI componentUri = component.getUri();
            if (componentUri.toString().startsWith(strigified)) {
                uris.add(componentUri);
            }
        }
        return uris;
    }
}
