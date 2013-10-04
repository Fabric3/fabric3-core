/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.java.generator;

import java.net.URI;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaSourceDefinition;
import org.fabric3.implementation.java.provision.JavaTargetDefinition;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.model.type.component.CallbackDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.generator.policy.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.Signature;

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

    public void generate(JavaComponentDefinition definition, LogicalComponent<? extends JavaImplementation> component) throws GenerationException {
        ComponentDefinition<? extends JavaImplementation> logical = component.getDefinition();
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


    public void generateWireSource(JavaSourceDefinition definition, LogicalReference reference, EffectivePolicy policy) throws GenerationException {
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
            DataType<?> type = reference.getDefinition().getKeyDataType();
            String className = type.getPhysical().getName();
            definition.setKeyClassName(className);
        }
    }

    public void generateConnectionSource(JavaConnectionSourceDefinition definition, LogicalProducer producer) throws GenerationException {
        URI uri = producer.getUri();
        ServiceContract serviceContract = producer.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        definition.setUri(uri);
        definition.setInjectable(new Injectable(InjectableType.PRODUCER, uri.getFragment()));
        definition.setInterfaceName(interfaceName);
    }

    @SuppressWarnings({"unchecked"})
    public void generateConnectionTarget(JavaConnectionTargetDefinition definition, LogicalConsumer consumer) throws GenerationException {
        LogicalComponent<? extends JavaImplementation> component = (LogicalComponent<? extends JavaImplementation>) consumer.getParent();
        // TODO support promotion by returning the leaf component URI instead of the parent component URI
        URI uri = component.getUri();
        definition.setTargetUri(uri);
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        Signature signature = type.getConsumerSignature(consumer.getUri().getFragment());
        if (signature == null) {
            // programming error
            throw new GenerationException("Consumer signature not found on: " + consumer.getUri());
        }
        definition.setConsumerSignature(signature);
    }

    public void generateCallbackWireSource(JavaSourceDefinition definition,
                                           LogicalComponent<? extends JavaImplementation> component,
                                           ServiceContract serviceContract,
                                           EffectivePolicy policy) throws GenerationException {
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (CallbackDefinition entry : type.getCallbacks().values()) {
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
            throw new CallbackSiteNotFoundException("Callback injection site not found for type: " + interfaze, interfaze);
        }

        Injectable injectable = new Injectable(InjectableType.CALLBACK, name);
        definition.setInjectable(injectable);
        definition.setInterfaceName(interfaceName);
        URI uri = URI.create(component.getUri().toString() + "#" + name);
        definition.setUri(uri);
        definition.setOptimizable(false);
    }

    public void generateResourceWireSource(JavaSourceDefinition wireDefinition, LogicalResourceReference<?> resourceReference)
            throws GenerationException {
        URI uri = resourceReference.getUri();
        ServiceContract serviceContract = resourceReference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();

        wireDefinition.setUri(uri);
        wireDefinition.setInjectable(new Injectable(InjectableType.RESOURCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
    }

    @SuppressWarnings({"unchecked"})
    public void generateWireTarget(JavaTargetDefinition definition, LogicalService service) throws GenerationException {
        LogicalComponent<JavaImplementation> component = (LogicalComponent<JavaImplementation>) service.getLeafComponent();
        URI uri = URI.create(component.getUri().toString() + "#" + service.getUri().getFragment());
        definition.setUri(uri);

        // assume only wires to composite scope components can be optimized
        ComponentDefinition<JavaImplementation> componentDefinition = component.getDefinition();
        JavaImplementation implementation = componentDefinition.getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        String scope = componentType.getScope();
        definition.setOptimizable(Scope.getScope(scope).isSingleton());
    }

}
