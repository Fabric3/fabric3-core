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
package org.fabric3.implementation.timer.generator;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.implementation.timer.model.TimerImplementation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.generator.JavaGenerationHelper;
import org.fabric3.implementation.java.provision.JavaConnectionSource;
import org.fabric3.implementation.java.provision.JavaWireSource;
import org.fabric3.implementation.timer.provision.TimerPhysicalComponent;
import org.fabric3.spi.domain.generator.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponent;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates physical metadata for a Timer component deployment.
 */
@EagerInit
public class TimerComponentGenerator implements ComponentGenerator<LogicalComponent<TimerImplementation>> {
    private static final String MANAGED_TRANSACTION = "managedTransaction";

    private JavaGenerationHelper generationHelper;

    public TimerComponentGenerator(@Reference JavaGenerationHelper generationHelper) {
        this.generationHelper = generationHelper;
    }

    public PhysicalComponent generate(LogicalComponent<TimerImplementation> component) throws Fabric3Exception {
        TimerPhysicalComponent physicalComponent = new TimerPhysicalComponent();
        generationHelper.generate(physicalComponent, component);
        TimerImplementation implementation = component.getDefinition().getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        physicalComponent.setTransactional(componentType.getPolicies().contains(MANAGED_TRANSACTION));
        TimerData data = implementation.getTimerData();
        physicalComponent.setTriggerData(data);
        return physicalComponent;
    }

    public PhysicalWireSource generateSource(LogicalReference reference) throws Fabric3Exception {
        JavaWireSource source = new JavaWireSource();
        generationHelper.generateWireSource(source, reference);
        return source;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireSource generateCallbackSource(LogicalService service) throws Fabric3Exception {
        JavaWireSource source = new JavaWireSource();
        JavaServiceContract callbackContract = (JavaServiceContract) service.getDefinition().getServiceContract().getCallbackContract();
        LogicalComponent<JavaImplementation> component = (LogicalComponent<JavaImplementation>) service.getParent();
        generationHelper.generateCallbackWireSource(source, component, callbackContract);
        return source;
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) throws Fabric3Exception {
        JavaConnectionSource source = new JavaConnectionSource();
        generationHelper.generateConnectionSource(source, producer);
        return source;
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) throws Fabric3Exception {
        JavaWireSource source = new JavaWireSource();
        generationHelper.generateResourceWireSource(source, resourceReference);
        return source;
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) throws Fabric3Exception {
        throw new UnsupportedOperationException("Timer components cannot be configured as event consumers");
    }

    public PhysicalWireTarget generateTarget(LogicalService service) throws Fabric3Exception {
        throw new UnsupportedOperationException("Cannot wire to timer components");
    }
}