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
package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.monitor.generator.MonitorResourceReferenceGenerator;
import org.fabric3.monitor.introspection.MonitorProcessor;
import org.fabric3.monitor.model.MonitorResourceReference;
import org.fabric3.monitor.provision.MonitorWireTargetDefinition;
import org.fabric3.monitor.runtime.MonitorServiceImpl;
import org.fabric3.monitor.runtime.MonitorWireAttacher;
import static org.fabric3.spi.model.type.system.SystemComponentBuilder.newBuilder;

/**
 * Provides core components for the monitor subsystem.
 */
public class MonitorProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "MonitorComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        compositeBuilder.component(newBuilder(MonitorProcessor.class).key(Monitor.class.getName()).build());
        compositeBuilder.component(newBuilder(MonitorResourceReferenceGenerator.class).key(MonitorResourceReference.class.getName()).build());
        compositeBuilder.component(newBuilder(MonitorWireAttacher.class).key(MonitorWireTargetDefinition.class.getName()).build());

        compositeBuilder.component(newBuilder(MonitorServiceImpl.class).build());

        return compositeBuilder.build();
    }

}
