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
package org.fabric3.fabric.domain;

import java.net.URI;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.type.component.CompositeImplementation;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation of LogicalComponentManager. The runtime domain configuration (created during bootstrap) defaults autowire to ON; the application domain
 * defaults autowire to OFF, which can be overridden by the runtime configuration.
 */
public class LogicalComponentManagerImpl implements LogicalComponentManager {
    private URI domainUri;
    private String autowireValue;
    private boolean autowire = true;
    private LogicalCompositeComponent domain;
    private LCMMonitor monitor;

    /**
     * Bootstrap constructor.
     */
    public LogicalComponentManagerImpl() {
        this.domainUri = Names.RUNTIME_URI;
        initializeDomainComposite();
    }

    @Constructor
    public LogicalComponentManagerImpl(@Reference HostInfo info) {
        domainUri = info.getDomain();
    }

    @Property(required = false)
    @Source("$systemConfig/f3:domain/@autowire")
    public void setAutowire(String value) {
        autowireValue = value;
    }

    @Monitor
    public void setMonitor(LCMMonitor monitor) {
        this.monitor = monitor;
    }

    @Init
    public void init() {
        if (autowireValue != null) {
            boolean autowire;
            // can't use Enum.valueOf(..) as INHERITED is not a valid value for the domain composite
            if ("ON".equalsIgnoreCase(autowireValue.trim())) {
                autowire = true;
            } else if ("OFF".equalsIgnoreCase(autowireValue.trim())) {
                autowire = false;
            } else {
                monitor.invalidAutowireValue(autowireValue);
                return;
            }
            this.autowire = autowire;
        }
        initializeDomainComposite();
    }

    public LogicalComponent<?> getComponent(URI uri) {
        String defragmentedUri = UriHelper.getDefragmentedNameAsString(uri);
        String domainString = domain.getUri().toString();
        String[] hierarchy = defragmentedUri.substring(domainString.length() + 1).split("/");
        String currentUri = domainString;
        LogicalComponent<?> currentComponent = domain;
        for (String name : hierarchy) {
            currentUri = currentUri + "/" + name;
            if (currentComponent instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) currentComponent;
                currentComponent = composite.getComponent(URI.create(currentUri));
            }
            if (currentComponent == null) {
                return null;
            }
        }
        return currentComponent;
    }

    public LogicalCompositeComponent getRootComponent() {
        return domain;
    }

    public void replaceRootComponent(LogicalCompositeComponent component) {
        domain = component;
    }

    private void initializeDomainComposite() {
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        Component<CompositeImplementation> definition = new Component<>(domainUri.toString());
        definition.setImplementation(impl);
        definition.setContributionUri(Names.BOOT_CONTRIBUTION);
        domain = new LogicalCompositeComponent(domainUri, definition, autowire);
        domain.setState(LogicalState.PROVISIONED);
    }

}
