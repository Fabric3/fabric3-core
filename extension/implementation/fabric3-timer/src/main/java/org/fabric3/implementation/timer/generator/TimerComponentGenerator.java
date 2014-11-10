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

import javax.xml.namespace.QName;

import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.java.generator.JavaGenerationHelper;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.api.implementation.timer.model.TimerImplementation;
import org.fabric3.implementation.timer.provision.TimerComponentDefinition;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.GenerationException;
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
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Generates physical metadata for a Timer component deployment.
 */
@EagerInit
public class TimerComponentGenerator implements ComponentGenerator<LogicalComponent<TimerImplementation>> {
    private static final QName MANAGED_TRANSACTION = new QName(Constants.SCA_NS, "managedTransaction");
    private JavaGenerationHelper generationHelper;

    public TimerComponentGenerator(@Reference JavaGenerationHelper generationHelper) {
        this.generationHelper = generationHelper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<TimerImplementation> component) throws GenerationException {
        TimerComponentDefinition definition = new TimerComponentDefinition();
        generationHelper.generate(definition, component);
        TimerImplementation implementation = component.getDefinition().getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        definition.setTransactional(implementation.getIntents().contains(MANAGED_TRANSACTION)
                                            || componentType.getIntents().contains(MANAGED_TRANSACTION));
        TimerData data = implementation.getTimerData();
        definition.setTriggerData(data);
        return definition;
    }

    public PhysicalWireSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        generationHelper.generateWireSource(definition, reference, policy);
        return definition;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        LogicalComponent<JavaImplementation> source = (LogicalComponent<JavaImplementation>) service.getLeafComponent();
        generationHelper.generateCallbackWireSource(definition, source, callbackContract, policy);
        return definition;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        JavaConnectionSourceDefinition definition = new JavaConnectionSourceDefinition();
        generationHelper.generateConnectionSource(definition, producer);
        return definition;
    }

    public PhysicalWireSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        generationHelper.generateResourceWireSource(definition, resourceReference);
        return definition;
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        throw new UnsupportedOperationException("Timer components cannot be configured as event consumers");
    }

    public PhysicalWireTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException("Cannot wire to timer components");
    }
}