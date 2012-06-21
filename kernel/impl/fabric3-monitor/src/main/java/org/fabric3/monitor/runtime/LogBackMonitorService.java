/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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


import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.component.ComponentBuilderListener;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.monitor.MonitorService;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
@Management(name = "MonitorService", path = "/runtime/monitor", description = "Sets monitoring levels for the runtime")
@Service(names = {MonitorService.class, ComponentBuilderListener.class})
public class LogBackMonitorService implements MonitorService, ComponentBuilderListener {
    private ComponentManager manager;
    private HostInfo info;
    private MonitorLevel defaultLevel = MonitorLevel.WARNING;
    private Map<URI, MonitorLevel> applicationComponentLevels = Collections.emptyMap();
    private Map<URI, MonitorLevel> runtimeComponentLevels = Collections.emptyMap();
    private Map<QName, MonitorLevel> deployableLevels = Collections.emptyMap();
    private Map<String,String> providerLevels = Collections.emptyMap();

    public LogBackMonitorService(@Reference ComponentManager manager, @Reference HostInfo info) {
        this.manager = manager;
        this.info = info;
    }

    @Property(required = false)
    public void setDefaultLevel(String defaultLevel) {
        this.defaultLevel = MonitorLevel.valueOf(defaultLevel);
    }

    /**
     * Used at runtime startup to set the monitor levels for application components under a URI. Components are specified using a relative URI, which
     * is made resolved against the application domain.
     *
     * @param levels the mapping of relative URI to monitor level.
     */
    @Property(required = false)
    public void setApplicationComponentLevels(Element levels) {
        this.applicationComponentLevels = new HashMap<URI, MonitorLevel>();
        // add the application domain prefix
        String base = info.getDomain().toString();
        NodeList list = levels.getElementsByTagName("level");
        for (int i = 0; i < list.getLength(); i++) {
            URI uri;
            Element element = (Element) list.item(i);
            String name = element.getAttribute("name");
            if (name.length() == 0) {
                // root domain component specified
                uri = info.getDomain();

            } else {
                uri = URI.create(base + "/" + name);
            }
            String value = element.getAttribute("value").toUpperCase();
            MonitorLevel level = MonitorLevel.valueOf(value);
            applicationComponentLevels.put(uri, level);

        }
    }

    /**
     * Used at runtime startup to set the monitor levels for runtime components under a URI. Components are specified using a relative URI, which is
     * made resolved against the runtime domain.
     *
     * @param levels the mapping of relative URI to monitor level.
     */
    @Property(required = false)
    public void setRuntimeComponentLevels(Element levels) {
        this.runtimeComponentLevels = new HashMap<URI, MonitorLevel>();
        NodeList list = levels.getElementsByTagName("level");
        for (int i = 0; i < list.getLength(); i++) {
            URI uri;
            Element element = (Element) list.item(i);
            String name = element.getAttribute("name");
            if (name.length() == 0) {
                // root domain component specified
                uri = Names.RUNTIME_URI;

            } else {
                uri = URI.create(Names.RUNTIME_NAME + "/" + name);
            }
            String value = element.getAttribute("value").toUpperCase();
            MonitorLevel level = MonitorLevel.valueOf(value);
            runtimeComponentLevels.put(uri, level);
        }
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
     * Used at runtime startup to set the monitor levels based on class names.
     *
     * @param levels the mapping of classes-loggers to monitor level
     */
    @Property(required = false)
    public void setLoggerLevels(Element levels){
         this.providerLevels = new HashMap<String, String>();
         NodeList list = levels.getElementsByTagName("level");
        for (int i=0; i < list.getLength();i++)
        {
            Element element = (Element) list.item(i);
            String className = element.getAttribute("name");
            String level = element.getAttribute("value");
            setProviderLevel(className,level);
        }
    }

    @Init
    public void init() {
        ch.qos.logback.classic.Level level = LevelConverter.getLogbackLevel(defaultLevel);
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(level);
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
        ch.qos.logback.classic.Level logBackLevel = LevelConverter.getLogbackLevel(parsed);
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(key)).setLevel(logBackLevel);

    }

    public void onBuild(Component component, PhysicalComponentDefinition definition) {
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