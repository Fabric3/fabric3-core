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
package org.fabric3.monitor.runtime;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.container.builder.component.ComponentBuilderListener;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
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
@Service({MonitorService.class, ComponentBuilderListener.class})
public class MonitorServiceImpl implements MonitorService, ComponentBuilderListener {
    private ComponentManager manager;
    private Map<URI, MonitorLevel> applicationComponentLevels = Collections.emptyMap();
    private Map<URI, MonitorLevel> runtimeComponentLevels = Collections.emptyMap();
    private Map<QName, MonitorLevel> deployableLevels = Collections.emptyMap();
    private Map<String, MonitorLevel> providerLevels = new ConcurrentHashMap<>();
    private Map<URI, MonitorLevel> contributionLevels = new ConcurrentHashMap<>();

    private MonitorLevel defaultLevel = MonitorLevel.INFO;

    public MonitorServiceImpl(@Reference ComponentManager manager, @Reference HostInfo info) {
        this.manager = manager;
        MonitorLocator.setInstance(this);
    }

    @Property(required = false)
    @Source("$systemConfig//f3:runtime/@monitor.level")
    public void setDefaultLevel(String defaultLevel) {
        try {
            this.defaultLevel = MonitorLevel.valueOf(defaultLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid monitor level value: " + defaultLevel);
        }
    }

    /**
     * Used at runtime startup to set the monitor levels for components contained in a deployable composite.
     *
     * @param levels the mapping of composite name to monitor level.
     */
    @Property(required = false)
    @Source("$systemConfig//f3:runtime/f3:monitor/f3:deployable.levels")
    public void setDeployableLevels(Element levels) {
        this.deployableLevels = new HashMap<>();
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
    @Source("$systemConfig//f3:runtime/f3:monitor/f3:contribution.levels")
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
    @Source("$systemConfig//f3:runtime/f3:monitor/f3:provider.levels")
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
        // set the default level of any components deployed before this
        for (Component component : manager.getComponents()) {
            component.setLevel(defaultLevel);
        }

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
        MonitorLevel parsed = MonitorLevel.valueOf(level.toUpperCase());
        List<Component> components = manager.getComponentsInHierarchy(URI.create(uri));
        for (Component component : components) {
            component.setLevel(parsed);
        }
    }

    @ManagementOperation(description = "Sets the monitoring level for a deployable composite")
    public void setDeployableLevel(String deployable, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level.toUpperCase());
        List<Component> components = manager.getDeployedComponents(QName.valueOf(deployable));
        for (Component component : components) {
            component.setLevel(parsed);
        }
    }

    @ManagementOperation(description = "Sets the monitoring level for a provider")
    public void setProviderLevel(String key, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level.toUpperCase());
        providerLevels.put(key, parsed);
    }

    public MonitorLevel getProviderLevel(String key) {
        return providerLevels.get(key);
    }

    @ManagementOperation(description = "Sets the monitoring level for a contribution or extension")
    public void setContributionLevel(String key, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level.toUpperCase());
        URI uri = URI.create(key);
        contributionLevels.put(uri, parsed);
        for (Component component : manager.getComponents()) {
            if (uri.equals(component.getContributionUri())) {
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

        level = contributionLevels.get(component.getContributionUri());
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