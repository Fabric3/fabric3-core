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
package org.fabric3.implementation.spring.generator;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.implementation.spring.model.SpringConsumer;
import org.fabric3.implementation.spring.model.SpringImplementation;
import org.fabric3.implementation.spring.model.SpringReference;
import org.fabric3.implementation.spring.model.SpringService;
import org.fabric3.implementation.spring.provision.SpringComponentDefinition;
import org.fabric3.implementation.spring.provision.SpringConnectionSourceDefinition;
import org.fabric3.implementation.spring.provision.SpringConnectionTargetDefinition;
import org.fabric3.implementation.spring.provision.SpringWireSourceDefinition;
import org.fabric3.implementation.spring.provision.SpringWireTargetDefinition;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
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
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
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
        Component<SpringImplementation> componentDefinition = component.getDefinition();
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

    public PhysicalWireSourceDefinition generateSource(LogicalReference reference) throws GenerationException {
        ServiceContract contract = reference.getLeafReference().getServiceContract();
        if (!(contract instanceof JavaServiceContract)) {
            // Spring reference contracts are always defined by Java interfaces
            throw new GenerationException("Unexpected interface type for " + reference.getUri() + ": " + contract.getClass().getName());
        }
        String interfaze = contract.getQualifiedInterfaceName();
        URI uri = reference.getParent().getUri();
        String referenceName = reference.getDefinition().getName();
        return new SpringWireSourceDefinition(referenceName, interfaze, uri);
    }

    public PhysicalWireTargetDefinition generateTarget(LogicalService service) throws GenerationException {
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
        return new SpringWireTargetDefinition(target, interfaceName, uri);
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
        JavaType type = springConsumer.getType();
        URI uri = consumer.getParent().getUri();
        return new SpringConnectionTargetDefinition(beanName, methodName, type, uri);
    }

    public PhysicalWireSourceDefinition generateCallbackSource(LogicalService service) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private Map<String, String> handleDefaultReferenceMappings(Component<SpringImplementation> component, ComponentType type) {
        Map<String, String> mappings = new HashMap<>();
        for (Reference reference : type.getReferences().values()) {
            SpringReference springReference = (SpringReference) reference;
            String defaultStr = springReference.getDefaultValue();
            if (defaultStr == null) {
                continue;
            }
            String refName = springReference.getName();
            if (component.getReferences().containsKey(refName)) {
                continue;
            }
            mappings.put(defaultStr, refName);
        }
        return mappings;
    }

    private void processPropertyValues(LogicalComponent<?> component, SpringComponentDefinition physical) {
        for (LogicalProperty property : component.getAllProperties().values()) {
            String name = property.getName();
            boolean many = property.isMany();
            if (property.getValue() != null) {
                Document document = property.getValue();
                ComponentType componentType = component.getDefinition().getImplementation().getComponentType();
                QName type = componentType.getProperties().get(property.getName()).getType();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, document, many, type);
                physical.setPropertyDefinition(definition);
            } else if (property.getInstanceValue() != null) {
                Object value = property.getInstanceValue();
                PhysicalPropertyDefinition definition = new PhysicalPropertyDefinition(name, value, many);
                physical.setPropertyDefinition(definition);
            }
        }
    }

}
