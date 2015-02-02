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
package org.fabric3.implementation.java.generator;

import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.fabric3.implementation.java.provision.JavaWireTargetDefinition;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
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
 * Generates physical metadata for a Java component deployment.
 */
@EagerInit
public class JavaComponentGenerator implements ComponentGenerator<LogicalComponent<JavaImplementation>> {
    protected final GenerationHelper ifHelper;
    private JavaGenerationHelper generationHelper;

    public JavaComponentGenerator(@Reference JavaGenerationHelper generationHelper, @Reference GenerationHelper ifHelper) {
        this.generationHelper = generationHelper;
        this.ifHelper = ifHelper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<JavaImplementation> component) {
        Object instance = component.getDefinition().getImplementation().getInstance();
        if (instance != null) {
            // deploying an un managed instance
            JavaComponentDefinition definition = new JavaComponentDefinition(instance);
            definition.setScope(Scope.COMPOSITE);
            return definition;
        }
        JavaComponentDefinition definition = new JavaComponentDefinition();
        generationHelper.generate(definition, component);
        return definition;
    }

    public PhysicalWireSourceDefinition generateSource(LogicalReference reference) {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        generationHelper.generateWireSource(definition, reference);
        return definition;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireSourceDefinition generateCallbackSource(LogicalService service) {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        LogicalComponent<JavaImplementation> source = (LogicalComponent<JavaImplementation>) service.getLeafComponent();
        generationHelper.generateCallbackWireSource(definition, source, callbackContract);
        return definition;
    }

    public PhysicalWireTargetDefinition generateTarget(LogicalService service) {
        JavaWireTargetDefinition definition = new JavaWireTargetDefinition();
        generationHelper.generateWireTarget(definition, service);
        return definition;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) {
        JavaConnectionSourceDefinition definition = new JavaConnectionSourceDefinition();
        generationHelper.generateConnectionSource(definition, producer);
        return definition;
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) {
        JavaConnectionTargetDefinition definition = new JavaConnectionTargetDefinition();
        generationHelper.generateConnectionTarget(definition, consumer);
        return definition;
    }

    public PhysicalWireSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) {
        JavaWireSourceDefinition definition = new JavaWireSourceDefinition();
        generationHelper.generateResourceWireSource(definition, resourceReference);
        return definition;
    }

}
