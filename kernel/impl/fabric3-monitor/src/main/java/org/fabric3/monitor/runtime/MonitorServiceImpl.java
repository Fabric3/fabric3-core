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
package org.fabric3.monitor.runtime;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.container.builder.component.ComponentBuilderListener;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.monitor.MonitorLocator;
import org.fabric3.spi.monitor.MonitorService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 */
@EagerInit
@Management(name = "MonitorService", path = "/runtime/monitor", description = "Sets monitoring levels for the runtime")
@Service(names = {MonitorService.class, ComponentBuilderListener.class})
public class MonitorServiceImpl implements MonitorService, ComponentBuilderListener {
    private ComponentManager manager;
    private Map<URI, MonitorLevel> applicationComponentLevels = Collections.emptyMap();
    private Map<URI, MonitorLevel> runtimeComponentLevels = Collections.emptyMap();
    private Map<QName, MonitorLevel> deployableLevels = Collections.emptyMap();
    private Map<String, MonitorLevel> providerLevels = new ConcurrentHashMap<String, MonitorLevel>();
    private Map<URI, MonitorLevel> contributionLevels = new ConcurrentHashMap<URI, MonitorLevel>();

    private MonitorLevel defaultLevel = MonitorLevel.INFO;

    public MonitorServiceImpl(@Reference ComponentManager manager, @Reference HostInfo info) {
        this.manager = manager;
        MonitorLocator.setInstance(this);
    }

    @Property(required = false)
    public void setDefaultLevel(String defaultLevel) {
        this.defaultLevel = MonitorLevel.valueOf(defaultLevel);
    }

    /**
     * Used at runtime startup to set the monitor levels for components contained in a deployable composite.
     *
     * @param levels the mapping of composite name to monitor level.
     */
    @Property(required = false)
    public void setDeployableLevels(Element levels) {
        this.deployableLevels = new HashMap<QName, MonitorLevel>();
        NodeList list = levels.getElementsByTagName("level");
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            String value = element.getAttribute("value").toUpperCase();
            MonitorLevel level = MonitorLevel.valueOf(value.toUpperCase());
            deployableLevels.put(getQualifiedName(element), level);
        }
    }

    /**
     * Used at runtime startup to set the monitor levels based on a contribution name.
     *
     * @param levels the mapping of classes-loggers to monitor level
     */
    @Property(required = false)
    public void setContributionLevels(Element levels) {
        NodeList list = levels.getElementsByTagName("level");
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            String name = element.getAttribute("name");
            String level = element.getAttribute("value");
            setContributionLevel(name, level);
        }
    }

    /**
     * Used at runtime startup to set the monitor levels based on a provider key. e.g a LogBack or Commons Logging key.
     *
     * @param levels the mapping of classes-loggers to monitor level
     */
    @Property(required = false)
    public void setProviderLevels(Element levels) {
        NodeList list = levels.getElementsByTagName("level");
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            String name = element.getAttribute("name");
            String level = element.getAttribute("value");
            setProviderLevel(name, level);
        }
    }

    @Init
    public void init() {
        for (Map.Entry<QName, MonitorLevel> entry : deployableLevels.entrySet()) {
            for (Component component : manager.getDeployedComponents(entry.getKey())) {
                component.setLevel(entry.getValue());
            }
        }
        for (Map.Entry<URI, MonitorLevel> entry : applicationComponentLevels.entrySet()) {
            for (Component component : manager.getComponentsInHierarchy(entry.getKey())) {
                component.setLevel(entry.getValue());
            }
        }
        for (Map.Entry<URI, MonitorLevel> entry : runtimeComponentLevels.entrySet()) {
            for (Component component : manager.getComponentsInHierarchy(entry.getKey())) {
                component.setLevel(entry.getValue());
            }
        }
    }

    @ManagementOperation(description = "Sets the monitoring level for a component")
    public void setComponentLevel(String uri, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level);
        List<Component> components = manager.getComponentsInHierarchy(URI.create(uri));
        for (Component component : components) {
            component.setLevel(parsed);
        }
    }

    @ManagementOperation(description = "Sets the monitoring level for a deployable composite")
    public void setDeployableLevel(String deployable, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level);
        List<Component> components = manager.getDeployedComponents(QName.valueOf(deployable));
        for (Component component : components) {
            component.setLevel(parsed);
        }
    }

    @ManagementOperation(description = "Sets the monitoring level for a provider")
    public void setProviderLevel(String key, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level);
        providerLevels.put(key, parsed);
    }

    public MonitorLevel getProviderLevel(String key) {
        return providerLevels.get(key);
    }

    @ManagementOperation(description = "Sets the monitoring level for a contribution or extension")
    private void setContributionLevel(String key, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level);
        URI uri = URI.create(key);
        contributionLevels.put(uri, parsed);
        for (Component component : manager.getComponents()) {
            if (uri.equals(component.getClassLoaderId())) {
                component.setLevel(parsed);
            }
        }
    }

    public void onBuild(Component component, PhysicalComponentDefinition definition) {
        if (MonitorLevel.INFO != defaultLevel) {
            component.setLevel(defaultLevel);
        }

        String strUri = component.getUri().toString();
        for (Map.Entry<URI, MonitorLevel> entry : runtimeComponentLevels.entrySet()) {
            if (strUri.startsWith(entry.getKey().toString())) {
                component.setLevel(entry.getValue());
                return;
            }
        }
        for (Map.Entry<URI, MonitorLevel> entry : applicationComponentLevels.entrySet()) {
            if (strUri.startsWith(entry.getKey().toString())) {
                component.setLevel(entry.getValue());
                return;
            }
        }
        MonitorLevel level = deployableLevels.get(component.getDeployable());
        if (level != null) {
            component.setLevel(level);
        }

        level = contributionLevels.get(component.getClassLoaderId());
        if (level != null) {
            component.setLevel(level);
        }

    }

    public void onDispose(Component component, PhysicalComponentDefinition definition) {
        // no-op
    }

    private QName getQualifiedName(Element element) {
        String text = element.getAttribute("name");
        int index = text.indexOf(':');
        if (index < 1 || index == text.length() - 1) {
            // unqualified form - use the default supplied
            return new QName(null, text);
        } else {
            String prefix = text.substring(0, index);
            String uri = element.lookupNamespaceURI(prefix);
            String localPart = text.substring(index + 1);
            return new QName(uri, localPart, prefix);
        }
    }
}