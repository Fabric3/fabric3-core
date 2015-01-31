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

import java.net.URI;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Callback;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.fabric3.implementation.java.provision.JavaWireTargetDefinition;
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

    public void generate(JavaComponentDefinition definition, LogicalComponent<? extends JavaImplementation> component) throws ContainerException {
        Component<? extends JavaImplementation> logical = component.getDefinition();
        JavaImplementation implementation = logical.getImplementation();
        InjectingComponentType type = implementation.getComponentType();
        String scope = type.getScope();

        // create the instance factory definition
        ImplementationManagerDefinition managerDefinition = new ImplementationManagerDefinition();
        managerDefinition.setComponentUri(component.getUri());
        managerDefinition.setReinjectable(Scope.COMPOSITE.getScope().equals(scope));
        managerDefinition.setConstructor(type.getConstructor());
        managerDefinition.setInitMethod(type.getInitMethod());
        managerDefinition.setDestroyMethod(type.getDestroyMethod());
        managerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(type, managerDefinition);

        // create the physical component definition
        definition.setScope(scope);
        definition.setEagerInit(type.isEagerInit());
        definition.setManagerDefinition(managerDefinition);

        definition.setManaged(type.isManaged());
        definition.setManagementInfo(type.getManagementInfo());

        helper.processPropertyValues(component, definition);
    }

    public void generateWireSource(JavaWireSourceDefinition definition, LogicalReference reference) throws ContainerException {
        URI uri = reference.getUri();
        ServiceContract serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();

        definition.setUri(uri);
        definition.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));
        definition.setInterfaceName(interfaceName);
        // assume for now that any wire from a Java component can be optimized
        definition.setOptimizable(true);

        if (reference.getDefinition().isKeyed()) {
            definition.setKeyed(true);
            DataType type = reference.getDefinition().getKeyDataType();
            String className = type.getType().getName();
            definition.setKeyClassName(className);
        }
    }

    public void generateConnectionSource(JavaConnectionSourceDefinition definition, LogicalProducer producer) throws ContainerException {
        URI uri = producer.getUri();
        ServiceContract serviceContract = producer.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        definition.setUri(uri);
        definition.setInjectable(new Injectable(InjectableType.PRODUCER, uri.getFragment()));
        definition.setInterfaceName(interfaceName);
    }

    @SuppressWarnings({"unchecked"})
    public void generateConnectionTarget(JavaConnectionTargetDefinition definition, LogicalConsumer consumer) throws ContainerException {
        LogicalComponent<? extends JavaImplementation> component = (LogicalComponent<? extends JavaImplementation>) consumer.getParent();
        // TODO support promotion by returning the leaf component URI instead of the parent component URI
        URI uri = component.getUri();
        definition.setUri(uri);
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        Signature signature = type.getConsumerSignature(consumer.getUri().getFragment());
        if (signature == null) {
            // programming error
            throw new ContainerException("Consumer signature not found on: " + consumer.getUri());
        }
        definition.setConsumerSignature(signature);
    }

    public void generateCallbackWireSource(JavaWireSourceDefinition definition,
                                           LogicalComponent<? extends JavaImplementation> component,
                                           ServiceContract serviceContract) throws ContainerException {
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (Callback entry : type.getCallbacks().values()) {
            // NB: This currently only supports the case where one callback injection site of the same type is on an implementation.
            // TODO clarify with the spec if having more than one callback injection site of the same type is valid
            ServiceContract candidate = entry.getServiceContract();
            MatchResult result = matcher.isAssignableFrom(candidate, serviceContract, false);
            if (result.isAssignable()) {
                name = entry.getName();
                break;
            }
        }
        if (name == null) {
            String interfaze = serviceContract.getQualifiedInterfaceName();
            throw new ContainerException("Callback injection site not found for type: " + interfaze);
        }

        Injectable injectable = new Injectable(InjectableType.CALLBACK, name);
        definition.setInjectable(injectable);
        definition.setInterfaceName(interfaceName);
        URI uri = URI.create(component.getUri().toString() + "#" + name);
        definition.setUri(uri);
        definition.setOptimizable(false);
    }

    public void generateResourceWireSource(JavaWireSourceDefinition wireDefinition, LogicalResourceReference<?> resourceReference) throws ContainerException {
        URI uri = resourceReference.getUri();
        ServiceContract serviceContract = resourceReference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();

        wireDefinition.setUri(uri);
        wireDefinition.setInjectable(new Injectable(InjectableType.RESOURCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
    }

    @SuppressWarnings({"unchecked"})
    public void generateWireTarget(JavaWireTargetDefinition definition, LogicalService service) throws ContainerException {
        LogicalComponent<JavaImplementation> component = (LogicalComponent<JavaImplementation>) service.getLeafComponent();
        URI uri = URI.create(component.getUri().toString() + "#" + service.getUri().getFragment());
        definition.setUri(uri);

        // assume only wires to composite scope components can be optimized
        Component<JavaImplementation> componentDefinition = component.getDefinition();
        JavaImplementation implementation = componentDefinition.getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        String scope = componentType.getScope();
        definition.setOptimizable(Scope.getScope(scope).isSingleton());
    }

}
