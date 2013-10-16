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
package org.fabric3.implementation.junit.generator;

import java.net.URI;

import org.fabric3.implementation.java.generator.JavaGenerationHelper;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaSourceDefinition;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.implementation.junit.provision.JUnitTargetDefinition;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.ServiceContract;
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
import org.fabric3.model.type.java.Injectable;
import org.fabric3.model.type.java.InjectableType;
import org.fabric3.model.type.java.InjectingComponentType;
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

    public PhysicalComponentDefinition generate(LogicalComponent<JUnitImplementation> component) throws GenerationException {

        ComponentDefinition<JUnitImplementation> definition = component.getDefinition();
        JUnitImplementation implementation = definition.getImplementation();
        InjectingComponentType type = implementation.getComponentType();
        String scope = type.getScope();

        ImplementationManagerDefinition managerDefinition = new ImplementationManagerDefinition();
        managerDefinition.setComponentUri(component.getUri());
        managerDefinition.setReinjectable(Scope.COMPOSITE.getScope().equals(scope));
        managerDefinition.setConstructor(type.getConstructor());
        managerDefinition.setInitMethod(type.getInitMethod());
        managerDefinition.setDestroyMethod(type.getDestroyMethod());
        managerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(type, managerDefinition);

        JavaComponentDefinition physical = new JavaComponentDefinition();

        physical.setScope(scope);
        physical.setManagerDefinition(managerDefinition);
        helper.processPropertyValues(component, physical);
        return physical;
    }

    public PhysicalSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);

        JavaSourceDefinition wireDefinition = new JavaSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);

        // assume for now that any wire from a JUnit component can be optimized
        wireDefinition.setOptimizable(true);

        if (reference.getDefinition().isKeyed()) {
            wireDefinition.setKeyed(true);
            DataType<?> type = reference.getDefinition().getKeyDataType();
            String className = type.getPhysical().getName();
            wireDefinition.setKeyClassName(className);
        }

        return wireDefinition;
    }

    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        JavaConnectionSourceDefinition definition = new JavaConnectionSourceDefinition();
        javaHelper.generateConnectionSource(definition, producer);
        return definition;
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        JavaConnectionTargetDefinition definition = new JavaConnectionTargetDefinition();
        javaHelper.generateConnectionTarget(definition, consumer);
        return definition;
    }


    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {

        URI uri = resourceReference.getUri();
        ServiceContract serviceContract = resourceReference.getDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);

        JavaSourceDefinition wireDefinition = new JavaSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setInjectable(new Injectable(InjectableType.RESOURCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        return wireDefinition;
    }

    private String getInterfaceName(ServiceContract contract) {
        return contract.getQualifiedInterfaceName();
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        JUnitTargetDefinition wireDefinition = new JUnitTargetDefinition();
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }
}
