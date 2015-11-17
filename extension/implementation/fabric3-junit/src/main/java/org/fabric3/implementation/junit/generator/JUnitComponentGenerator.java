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
package org.fabric3.implementation.junit.generator;

import java.net.URI;

import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.java.generator.JavaGenerationHelper;
import org.fabric3.implementation.java.provision.PhysicalJavaComponent;
import org.fabric3.implementation.java.provision.JavaConnectionSource;
import org.fabric3.implementation.java.provision.JavaConnectionTarget;
import org.fabric3.implementation.java.provision.JavaWireSource;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.implementation.junit.provision.JUnitWireTarget;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
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
 *
 */
@EagerInit
public class JUnitComponentGenerator implements ComponentGenerator<LogicalComponent<JUnitImplementation>> {
    private GenerationHelper helper;
    private JavaGenerationHelper javaHelper;

    public JUnitComponentGenerator(@Reference GenerationHelper helper, @Reference JavaGenerationHelper javaHelper) {
        this.helper = helper;
        this.javaHelper = javaHelper;
    }

    public PhysicalComponent generate(LogicalComponent<JUnitImplementation> component) {

        Component<JUnitImplementation> definition = component.getDefinition();
        JUnitImplementation implementation = definition.getImplementation();
        InjectingComponentType type = implementation.getComponentType();
        Scope scope = type.getScope();

        ImplementationManagerDefinition managerDefinition = new ImplementationManagerDefinition();
        managerDefinition.setReinjectable(Scope.COMPOSITE == scope);
        managerDefinition.setConstructor(type.getConstructor());
        managerDefinition.setInitMethod(type.getInitMethod());
        managerDefinition.setDestroyMethod(type.getDestroyMethod());
        managerDefinition.setImplementationClass(implementation.getImplementationClass());
        managerDefinition.setClassLoaderUri(definition.getContributionUri());
        helper.processInjectionSites(type, managerDefinition);

        PhysicalJavaComponent physical = new PhysicalJavaComponent();

        physical.setScope(scope);
        physical.setManagerDefinition(managerDefinition);
        helper.processPropertyValues(component, physical);
        return physical;
    }

    public PhysicalWireSource generateSource(LogicalReference reference) {
        URI uri = reference.getUri();
        JavaServiceContract serviceContract = (JavaServiceContract) reference.getDefinition().getServiceContract();

        JavaWireSource source = new JavaWireSource();
        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));
        source.setInterfaceClass(serviceContract.getInterfaceClass());

        // assume for now that any wire from a JUnit component can be optimized
        source.setOptimizable(true);

        if (reference.getDefinition().isKeyed()) {
            source.setKeyed(true);
            DataType type = reference.getDefinition().getKeyDataType();
            String className = type.getType().getName();
            source.setKeyClassName(className);
        }

        return source;
    }

    public PhysicalWireSource generateCallbackSource(LogicalService service) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        JavaConnectionSource source = new JavaConnectionSource();
        javaHelper.generateConnectionSource(source, producer);
        return source;
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) {
        JavaConnectionTarget target = new JavaConnectionTarget();
        javaHelper.generateConnectionTarget(target, consumer);
        return target;
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) {
        URI uri = resourceReference.getUri();
        JavaServiceContract serviceContract = (JavaServiceContract) resourceReference.getDefinition().getServiceContract();

        JavaWireSource source = new JavaWireSource();
        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.RESOURCE, uri.getFragment()));
        source.setInterfaceClass(serviceContract.getInterfaceClass());
        return source;
    }

    public PhysicalWireTarget generateTarget(LogicalService service) {
        JUnitWireTarget target = new JUnitWireTarget();
        target.setUri(service.getUri());
        return target;
    }
}
