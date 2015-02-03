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
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.generator.JavaGenerationHelper;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.fabric3.implementation.timer.provision.TimerComponentDefinition;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
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

    public PhysicalComponentDefinition generate(LogicalComponent<TimerImplementation> component) throws Fabric3Exception {
        TimerComponentDefinition definition = new TimerComponentDefinition();
        generationHelper.generate(definition, component);
        TimerImplementation implementation = component.getDefinition().getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        definition.setTransactional(componentType.getPolicies().contains(MANAGED_TRANSACTION));
        TimerData data = implementation.getTimerData();
        definition.setTriggerData(data);
        return definition;
    }

    public PhysicalWireSourceDefinition generateSource(LogicalReference reference) throws Fabric3Exception {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        generationHelper.generateWireSource(definition, reference);
        return definition;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireSourceDefinition generateCallbackSource(LogicalService service) throws Fabric3Exception {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        LogicalComponent<JavaImplementation> source = (LogicalComponent<JavaImplementation>) service.getParent();
        generationHelper.generateCallbackWireSource(definition, source, callbackContract);
        return definition;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws Fabric3Exception {
        JavaConnectionSourceDefinition definition = new JavaConnectionSourceDefinition();
        generationHelper.generateConnectionSource(definition, producer);
        return definition;
    }

    public PhysicalWireSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws Fabric3Exception {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        generationHelper.generateResourceWireSource(definition, resourceReference);
        return definition;
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws Fabric3Exception {
        throw new UnsupportedOperationException("Timer components cannot be configured as event consumers");
    }

    public PhysicalWireTargetDefinition generateTarget(LogicalService service) throws Fabric3Exception {
        throw new UnsupportedOperationException("Cannot wire to timer components");
    }
}