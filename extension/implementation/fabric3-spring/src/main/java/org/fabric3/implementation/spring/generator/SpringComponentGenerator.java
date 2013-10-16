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
package org.fabric3.implementation.spring.generator;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.implementation.spring.model.SpringConsumer;
import org.fabric3.implementation.spring.model.SpringImplementation;
import org.fabric3.implementation.spring.model.SpringReferenceDefinition;
import org.fabric3.implementation.spring.model.SpringService;
import org.fabric3.implementation.spring.provision.SpringComponentDefinition;
import org.fabric3.implementation.spring.provision.SpringConnectionSourceDefinition;
import org.fabric3.implementation.spring.provision.SpringConnectionTargetDefinition;
import org.fabric3.implementation.spring.provision.SpringSourceDefinition;
import org.fabric3.implementation.spring.provision.SpringTargetDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.generator.component.ComponentGenerator;
import org.fabric3.spi.deployment.generator.policy.EffectivePolicy;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.annotation.EagerInit;
import org.w3c.dom.Document;

/**
 * Generator for Spring components.
 */
@EagerInit
public class SpringComponentGenerator implements ComponentGenerator<LogicalComponent<SpringImplementation>> {

    public PhysicalComponentDefinition generate(LogicalComponent<SpringImplementation> component) throws GenerationException {
        URI uri = component.getUri();
        ComponentDefinition<SpringImplementation> componentDefinition = component.getDefinition();
        SpringImplementation implementation = componentDefinition.getImplementation();

        // if the app context is in a jar, calculate the base location, otherwise it is null
        String baseLocation = null;
        List<String> contextLocations = implementation.getContextLocations();
        if (SpringImplementation.LocationType.JAR == implementation.getLocationType()
            || SpringImplementation.LocationType.DIRECTORY == implementation.getLocationType()) {
            baseLocation = implementation.getLocation();
        }

        ComponentType type = componentDefinition.getComponentType();
        Map<String, String> mappings = handleDefaultReferenceMappings(componentDefinition, type);

        SpringComponentDefinition.LocationType locationType = SpringComponentDefinition.LocationType.valueOf(implementation.getLocationType().toString());

        SpringComponentDefinition physical = new SpringComponentDefinition(uri, baseLocation, contextLocations, mappings, locationType);
        processPropertyValues(component, physical);
        return physical;
    }

    public PhysicalSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        ServiceContract contract = reference.getLeafReference().getServiceContract();
        if (!(contract instanceof JavaServiceContract)) {
            // Spring reference contracts are always defined by Java interfaces
            throw new GenerationException("Unexpected interface type for " + reference.getUri() + ": " + contract.getClass().getName());
        }
        String interfaze = contract.getQualifiedInterfaceName();
        URI uri = reference.getParent().getUri();
        String referenceName = reference.getDefinition().getName();
        return new SpringSourceDefinition(referenceName, interfaze, uri);
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        if (!(service.getLeafService().getDefinition() instanceof SpringService)) {
            // programming error
            throw new GenerationException("Expected service type: " + service.getDefinition().getClass().getName());
        }
        SpringService springService = (SpringService) service.getLeafService().getDefinition();
        ServiceContract contract = springService.getServiceContract();
        if (!(contract instanceof JavaServiceContract)) {
            // Spring service contracts are always defined by Java interfaces
            throw new GenerationException("Unexpected interface type for " + service.getUri() + ": " + contract.getClass().getName());
        }

        String target = springService.getTarget();
        String interfaceName = contract.getQualifiedInterfaceName();
        URI uri = service.getUri();
        return new SpringTargetDefinition(target, interfaceName, uri);
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        String producerName = producer.getDefinition().getName();
        URI uri = producer.getParent().getUri();
        ServiceContract serviceContract = producer.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        return new SpringConnectionSourceDefinition(producerName, interfaceName, uri);
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        SpringConsumer springConsumer = (SpringConsumer) consumer.getDefinition();
        String beanName = springConsumer.getBeanName();
        String methodName = springConsumer.getMethodName();
        JavaType<?> type = springConsumer.getType();
        URI uri = consumer.getParent().getUri();
        return new SpringConnectionTargetDefinition(beanName, methodName, type, uri);
    }

    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private Map<String, String> handleDefaultReferenceMappings(ComponentDefinition<SpringImplementation> componentDefinition, ComponentType type) {
        Map<String, String> mappings = new HashMap<String, String>();
        for (ReferenceDefinition reference : type.getReferences().values()) {
            SpringReferenceDefinition springReference = (SpringReferenceDefinition) reference;
            String defaultStr = springReference.getDefaultValue();
            if (defaultStr == null) {
                continue;
            }
            String refName = springReference.getName();
            if (componentDefinition.getReferences().containsKey(refName)) {
                continue;
            }
            mappings.put(defaultStr, refName);
        }
        return mappings;
    }

    private void processPropertyValues(LogicalComponent<?> component, SpringComponentDefinition physical) {
        for (LogicalProperty property : component.getAllProperties().values()) {
            Document document = property.getValue();
            if (document != null) {
                String name = property.getName();
                boolean many = property.isMany();
                ComponentType componentType = component.getDefinition().getImplementation().getComponentType();
                QName type = componentType.getProperties().get(property.getName()).getType();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, document, many, type);
                physical.setPropertyDefinition(definition);
            }
        }
    }

}
