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
package org.fabric3.implementation.system.generator;

import java.lang.reflect.AccessibleObject;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.system.provision.PhysicalSystemComponent;
import org.fabric3.implementation.system.provision.SystemConnectionSource;
import org.fabric3.implementation.system.provision.SystemConnectionTarget;
import org.fabric3.implementation.system.provision.SystemWireSource;
import org.fabric3.implementation.system.provision.SystemWireTarget;
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
import org.fabric3.spi.model.type.system.SystemImplementation;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class SystemComponentGenerator implements ComponentGenerator<LogicalComponent<SystemImplementation>> {

    private final GenerationHelper helper;

    public SystemComponentGenerator(@Reference GenerationHelper helper) {
        this.helper = helper;
    }

    public PhysicalComponent generate(LogicalComponent<SystemImplementation> component) throws Fabric3Exception {
        Component<SystemImplementation> definition = component.getDefinition();
        SystemImplementation implementation = definition.getImplementation();
        InjectingComponentType type = implementation.getComponentType();

        ImplementationManagerDefinition managerDefinition = new ImplementationManagerDefinition();
        managerDefinition.setReinjectable(true);
        managerDefinition.setConstructor(type.getConstructor());
        managerDefinition.setInitMethod(type.getInitMethod());
        managerDefinition.setDestroyMethod(type.getDestroyMethod());
        managerDefinition.setImplementationClass(implementation.getImplementationClass());
        managerDefinition.setClassLoaderUri(definition.getContributionUri());
        helper.processInjectionSites(type, managerDefinition);

        // create the physical component definition
        PhysicalSystemComponent physical = new PhysicalSystemComponent();
        physical.setEagerInit(type.isEagerInit());

        physical.setManaged(type.isManaged());
        physical.setManagementInfo(type.getManagementInfo());

        physical.setManagerDefinition(managerDefinition);
        helper.processPropertyValues(component, physical);

        return physical;
    }

    public PhysicalWireSource generateSource(LogicalReference reference) throws Fabric3Exception {
        URI uri = reference.getUri();
        SystemWireSource definition = new SystemWireSource();
        definition.setOptimizable(true);
        definition.setUri(uri);
        definition.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));
        JavaServiceContract serviceContract = (JavaServiceContract) reference.getDefinition().getServiceContract();
        definition.setInterfaceClass(serviceContract.getInterfaceClass());

        if (reference.getDefinition().isKeyed()) {
            definition.setKeyed(true);
            String className = reference.getDefinition().getKeyDataType().getType().getName();
            definition.setKeyClassName(className);
        }

        return definition;
    }

    public PhysicalWireSource generateCallbackSource(LogicalService service) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTarget generateTarget(LogicalService service) throws Fabric3Exception {
        SystemWireTarget target = new SystemWireTarget();
        target.setOptimizable(true);
        target.setUri(service.getUri());
        return target;
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        SystemConnectionSource source = new SystemConnectionSource();
        URI uri = producer.getUri();
        JavaServiceContract serviceContract = (JavaServiceContract) producer.getDefinition().getServiceContract();
        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.PRODUCER, uri.getFragment()));
        source.setServiceInterface(serviceContract.getInterfaceClass());
        return source;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) throws Fabric3Exception {
        SystemConnectionTarget target = new SystemConnectionTarget();
        LogicalComponent<? extends SystemImplementation> component = (LogicalComponent<? extends SystemImplementation>) consumer.getParent();
        URI uri = component.getUri();
        target.setUri(uri);
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        AccessibleObject object = type.getConsumerSite(consumer.getUri().getFragment());
        if (object == null) {
            // programming error
            throw new Fabric3Exception("Consumer not found on: " + consumer.getUri());
        }
        target.setConsumerSite(object);
        return target;
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) throws Fabric3Exception {
        URI uri = resourceReference.getUri();
        SystemWireSource definition = new SystemWireSource();
        definition.setOptimizable(true);
        definition.setUri(uri);
        String name = uri.getFragment();
        Injectable injectable = new Injectable(InjectableType.RESOURCE, name);
        definition.setInjectable(injectable);
        return definition;
    }

}
