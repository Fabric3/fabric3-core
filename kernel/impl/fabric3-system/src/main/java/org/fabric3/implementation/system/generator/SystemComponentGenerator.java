/* 
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version, with the following exception:
 *
 * Linking this software statically or dynamically with other modules is making a combined
 * work based on this software. Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software give you permission to
 * link this software with independent modules to produce an executable, regardless of the
 * license terms of these independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that module. An independent
 * module is a module which is not derived from or based on this software. If you modify this
 * software, you may extend this exception to your version of the software, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 *
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.system.generator;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.spi.model.type.system.SystemImplementation;
import org.fabric3.implementation.system.provision.SystemComponentDefinition;
import org.fabric3.implementation.system.provision.SystemConnectionSourceDefinition;
import org.fabric3.implementation.system.provision.SystemConnectionTargetDefinition;
import org.fabric3.implementation.system.provision.SystemSourceDefinition;
import org.fabric3.implementation.system.provision.SystemTargetDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.generator.component.ComponentGenerator;
import org.fabric3.spi.deployment.generator.policy.EffectivePolicy;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.Signature;

/**
 *
 */
@EagerInit
public class SystemComponentGenerator implements ComponentGenerator<LogicalComponent<SystemImplementation>> {

    private final GenerationHelper helper;

    public SystemComponentGenerator(@Reference GenerationHelper helper) {
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<SystemImplementation> component) throws GenerationException {
        ComponentDefinition<SystemImplementation> definition = component.getDefinition();
        SystemImplementation implementation = definition.getImplementation();
        InjectingComponentType type = implementation.getComponentType();

        ImplementationManagerDefinition managerDefinition = new ImplementationManagerDefinition();
        managerDefinition.setComponentUri(component.getUri());
        managerDefinition.setReinjectable(true);
        managerDefinition.setConstructor(type.getConstructor());
        managerDefinition.setInitMethod(type.getInitMethod());
        managerDefinition.setDestroyMethod(type.getDestroyMethod());
        managerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(type, managerDefinition);

        // create the physical component definition
        SystemComponentDefinition physical = new SystemComponentDefinition();
        physical.setEagerInit(type.isEagerInit());

        physical.setManaged(type.isManaged());
        physical.setManagementInfo(type.getManagementInfo());

        physical.setManagerDefinition(managerDefinition);
        helper.processPropertyValues(component, physical);

        return physical;
    }

    public PhysicalSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        URI uri = reference.getUri();
        SystemSourceDefinition definition = new SystemSourceDefinition();
        definition.setOptimizable(true);
        definition.setUri(uri);
        definition.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));
        ServiceContract serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        definition.setInterfaceName(interfaceName);

        if (reference.getDefinition().isKeyed()) {
            definition.setKeyed(true);
            String className = reference.getDefinition().getKeyDataType().getType().getName();
            definition.setKeyClassName(className);
        }

        return definition;
    }

    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        SystemTargetDefinition definition = new SystemTargetDefinition();
        definition.setOptimizable(true);
        definition.setUri(service.getUri());
        return definition;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) {
        SystemConnectionSourceDefinition definition = new SystemConnectionSourceDefinition();
        URI uri = producer.getUri();
        ServiceContract serviceContract = producer.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        definition.setUri(uri);
        definition.setInjectable(new Injectable(InjectableType.PRODUCER, uri.getFragment()));
        definition.setInterfaceName(interfaceName);
        return definition;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        SystemConnectionTargetDefinition definition = new SystemConnectionTargetDefinition();
        LogicalComponent<? extends SystemImplementation> component = (LogicalComponent<? extends SystemImplementation>) consumer.getParent();
        URI uri = component.getUri();
        definition.setTargetUri(uri);
        InjectingComponentType type = component.getDefinition().getImplementation().getComponentType();
        Signature signature = type.getConsumerSignature(consumer.getUri().getFragment());
        if (signature == null) {
            // programming error
            throw new GenerationException("Consumer signature not found on: " + consumer.getUri());
        }
        definition.setConsumerSignature(signature);
        return definition;
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        URI uri = resourceReference.getUri();
        SystemSourceDefinition definition = new SystemSourceDefinition();
        definition.setOptimizable(true);
        definition.setUri(uri);
        String name = uri.getFragment();
        Injectable injectable = new Injectable(InjectableType.RESOURCE, name);
        definition.setInjectable(injectable);
        return definition;
    }


}
