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

import java.lang.reflect.AccessibleObject;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Callback;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaConnectionSource;
import org.fabric3.implementation.java.provision.JavaConnectionTarget;
import org.fabric3.implementation.java.provision.JavaWireSource;
import org.fabric3.implementation.java.provision.JavaWireTarget;
import org.fabric3.implementation.java.provision.PhysicalJavaComponent;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JavaGenerationHelperImpl implements JavaGenerationHelper {
    private final GenerationHelper helper;
    private ContractMatcher matcher;

    public JavaGenerationHelperImpl(@Reference GenerationHelper helper, @Reference ContractMatcher matcher) {
        this.helper = helper;
        this.matcher = matcher;
    }

    public void generate(PhysicalJavaComponent physicalComponent, LogicalComponent<? extends JavaImplementation> component) {
        Component<? extends JavaImplementation> logical = component.getDefinition();
        JavaImplementation implementation = logical.getImplementation();
        InjectingComponentType type = implementation.getComponentType();
        Scope scope = type.getScope();

        // create the instance factory definition
        ImplementationManagerDefinition managerDefinition = new ImplementationManagerDefinition();
        managerDefinition.setReinjectable(Scope.COMPOSITE == scope);
        managerDefinition.setConstructor(type.getConstructor());
        managerDefinition.setInitMethod(type.getInitMethod());
        managerDefinition.setDestroyMethod(type.getDestroyMethod());
        managerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(type, managerDefinition);

        // create the physical component definition
        physicalComponent.setScope(scope);
        physicalComponent.setEagerInit(type.isEagerInit());
        physicalComponent.setManagerDefinition(managerDefinition);

        physicalComponent.setManaged(type.isManaged());
        physicalComponent.setManagementInfo(type.getManagementInfo());

        helper.processPropertyValues(component, physicalComponent);
    }

    public void generateWireSource(JavaWireSource source, LogicalReference reference) {
        URI uri = reference.getUri();
        JavaServiceContract serviceContract = (JavaServiceContract) reference.getDefinition().getServiceContract();

        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));
        source.setInterfaceClass(serviceContract.getInterfaceClass());
        // assume for now that any wire from a Java component can be optimized
        source.setOptimizable(true);

        if (reference.getDefinition().isKeyed()) {
            source.setKeyed(true);
            DataType type = reference.getDefinition().getKeyDataType();
            String className = type.getType().getName();
            source.setKeyClassName(className);
        }
    }

    public void generateConnectionSource(JavaConnectionSource source, LogicalProducer producer) {
        URI uri = producer.getUri();
        source.setUri(uri);
        ServiceContract serviceContract = producer.getDefinition().getServiceContract();
        Class<?> interfaze = serviceContract.getInterfaceClass();
        source.setInjectable(new Injectable(InjectableType.PRODUCER, uri.getFragment()));
        source.setServiceInterface(interfaze);
    }

    @SuppressWarnings({"unchecked"})
    public void generateConnectionTarget(JavaConnectionTarget target, LogicalConsumer consumer) {
        // Create an injectable for the consumer. Note in cases where the consumer is a method that is connected via an event stream and is used to receive
        // events rather than act as a setter for a direct connection, the injector will never be activated.
        Injectable injectable = new Injectable(InjectableType.CONSUMER, consumer.getUri().getFragment());
        target.setInjectable(injectable);

        LogicalComponent<? extends JavaImplementation> component = (LogicalComponent<? extends JavaImplementation>) consumer.getParent();
        URI uri = component.getUri();
        target.setUri(uri);
        ServiceContract serviceContract = consumer.getDefinition().getServiceContract();
        Class<?> interfaze = serviceContract.getInterfaceClass();
        target.setServiceInterface(interfaze);

        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        AccessibleObject object = type.getConsumerSite(consumer.getUri().getFragment());
        if (object == null) {
            // programming error
            throw new Fabric3Exception("Consumer not found on: " + consumer.getUri());
        }

        target.setConsumerSite(object);
    }

    public void generateCallbackWireSource(JavaWireSource source, LogicalComponent<? extends JavaImplementation> component, JavaServiceContract contract) {
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (Callback entry : type.getCallbacks().values()) {
            // NB: This currently only supports the case where one callback injection site of the same type is on an implementation.
            ServiceContract candidate = entry.getServiceContract();
            MatchResult result = matcher.isAssignableFrom(candidate, contract, false);
            if (result.isAssignable()) {
                name = entry.getName();
                break;
            }
        }
        if (name == null) {
            String interfaze = contract.getQualifiedInterfaceName();
            throw new Fabric3Exception("Callback injection site not found for type: " + interfaze);
        }

        Injectable injectable = new Injectable(InjectableType.CALLBACK, name);
        source.setInjectable(injectable);
        source.setInterfaceClass(contract.getInterfaceClass());
        URI uri = URI.create(component.getUri().toString() + "#" + name);
        source.setUri(uri);
        source.setOptimizable(false);
    }

    public void generateResourceWireSource(JavaWireSource source, LogicalResourceReference<?> resourceReference) {
        URI uri = resourceReference.getUri();
        JavaServiceContract serviceContract = (JavaServiceContract) resourceReference.getDefinition().getServiceContract();

        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.RESOURCE, uri.getFragment()));
        source.setInterfaceClass(serviceContract.getInterfaceClass());
    }

    @SuppressWarnings({"unchecked"})
    public void generateWireTarget(JavaWireTarget target, LogicalService service) {
        LogicalComponent<JavaImplementation> component = (LogicalComponent<JavaImplementation>) service.getParent();
        URI uri = URI.create(component.getUri().toString() + "#" + service.getUri().getFragment());
        target.setUri(uri);

        // assume only wires to composite scope components can be optimized
        Component<JavaImplementation> componentDefinition = component.getDefinition();
        JavaImplementation implementation = componentDefinition.getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        Scope scope = componentType.getScope();
        target.setOptimizable(scope.isSingleton());
    }

}
