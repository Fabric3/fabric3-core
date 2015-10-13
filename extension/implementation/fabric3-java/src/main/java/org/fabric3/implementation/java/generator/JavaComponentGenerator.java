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
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.PhysicalJavaComponent;
import org.fabric3.implementation.java.provision.JavaConnectionSource;
import org.fabric3.implementation.java.provision.JavaConnectionTarget;
import org.fabric3.implementation.java.provision.JavaWireSource;
import org.fabric3.implementation.java.provision.JavaWireTarget;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
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

    public PhysicalComponent generate(LogicalComponent<JavaImplementation> component) {
        Object instance = component.getDefinition().getImplementation().getInstance();
        if (instance != null) {
            // deploying an unmanaged instance
            PhysicalJavaComponent physicalComponent = new PhysicalJavaComponent(instance);
            physicalComponent.setScope(Scope.COMPOSITE);
            return physicalComponent;
        }
        PhysicalJavaComponent physicalComponent = new PhysicalJavaComponent();
        generationHelper.generate(physicalComponent, component);
        return physicalComponent;
    }

    public PhysicalWireSource generateSource(LogicalReference reference) {
        JavaWireSource source = new JavaWireSource();
        generationHelper.generateWireSource(source, reference);
        return source;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireSource generateCallbackSource(LogicalService service) {
        JavaWireSource source = new JavaWireSource();
        JavaServiceContract callbackContract = (JavaServiceContract) service.getDefinition().getServiceContract().getCallbackContract();
        LogicalComponent<JavaImplementation> component = (LogicalComponent<JavaImplementation>) service.getParent();
        generationHelper.generateCallbackWireSource(source, component, callbackContract);
        return source;
    }

    public PhysicalWireTarget generateTarget(LogicalService service) {
        JavaWireTarget target = new JavaWireTarget();
        generationHelper.generateWireTarget(target, service);
        return target;
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        JavaConnectionSource source = new JavaConnectionSource();
        generationHelper.generateConnectionSource(source, producer);
        return source;
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) {
        JavaConnectionTarget target = new JavaConnectionTarget();
        generationHelper.generateConnectionTarget(target, consumer);
        return target;
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) {
        JavaWireSource source = new JavaWireSource();
        generationHelper.generateResourceWireSource(source, resourceReference);
        return source;
    }

}
