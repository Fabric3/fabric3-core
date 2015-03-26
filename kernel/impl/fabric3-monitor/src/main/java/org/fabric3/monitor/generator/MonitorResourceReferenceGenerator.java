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
package org.fabric3.monitor.generator;

import java.net.URI;

import org.fabric3.monitor.model.MonitorResourceReference;
import org.fabric3.monitor.provision.MonitorWireTarget;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class MonitorResourceReferenceGenerator implements ResourceReferenceGenerator<MonitorResourceReference> {

    public MonitorWireTarget generateWireTarget(LogicalResourceReference<MonitorResourceReference> resourceReference) {
        LogicalComponent<?> component = resourceReference.getParent();
        Class<?> type = ((JavaServiceContract)resourceReference.getDefinition().getServiceContract()).getInterfaceClass();
        URI monitorable = component.getUri();
        String destination = resourceReference.getDefinition().getDestination();
        MonitorWireTarget definition = new MonitorWireTarget(type, monitorable, destination);
        definition.setOptimizable(true);
        return definition;
    }
}
