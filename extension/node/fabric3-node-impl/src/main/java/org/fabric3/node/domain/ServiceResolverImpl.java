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
package org.fabric3.node.domain;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.HostNamespaces;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.node.NotFoundException;
import org.fabric3.node.nonmanaged.NonManagedImplementation;
import org.fabric3.node.nonmanaged.NonManagedPhysicalWireSourceDefinition;
import org.fabric3.spi.container.builder.Connector;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.domain.generator.binding.BindingSelector;
import org.fabric3.spi.domain.generator.wire.WireGenerator;
import org.fabric3.spi.domain.instantiator.AutowireResolver;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class ServiceResolverImpl implements ServiceResolver {
    private static final QName SYNTHETIC_DEPLOYABLE = new QName(HostNamespaces.SYNTHESIZED, "Synthetic");

    private Introspector introspector;
    private LogicalComponentManager lcm;
    private AutowireResolver autowireResolver;
    private BindingSelector bindingSelector;
    private WireGenerator wireGenerator;
    private Connector connector;
    private HostInfo info;
    private AtomicInteger idCounter = new AtomicInteger();

    public ServiceResolverImpl(@org.oasisopen.sca.annotation.Reference Introspector introspector,
                               @org.oasisopen.sca.annotation.Reference(name = "lcm") LogicalComponentManager lcm,
                               @org.oasisopen.sca.annotation.Reference AutowireResolver autowireResolver,
                               @org.oasisopen.sca.annotation.Reference BindingSelector bindingSelector,
                               @org.oasisopen.sca.annotation.Reference WireGenerator wireGenerator,
                               @org.oasisopen.sca.annotation.Reference Connector connector,
                               @org.oasisopen.sca.annotation.Reference HostInfo info) {
        this.introspector = introspector;
        this.lcm = lcm;
        this.autowireResolver = autowireResolver;
        this.bindingSelector = bindingSelector;
        this.wireGenerator = wireGenerator;
        this.connector = connector;
        this.info = info;
    }

    public <T> T resolve(Class<T> interfaze) throws Fabric3Exception {
        LogicalWire wire = createWire(interfaze);
        boolean remote = !wire.getSource().getParent().getZone().equals(wire.getTarget().getParent().getZone());

        PhysicalWireDefinition pwd;

        if (remote) {
            bindingSelector.selectBinding(wire);
            pwd = wireGenerator.generateBoundReference(wire.getSourceBinding());
            pwd.getSource().setUri(wire.getSource().getParent().getUri());
        } else {
            pwd = wireGenerator.generateWire(wire);
        }

        NonManagedPhysicalWireSourceDefinition source = (NonManagedPhysicalWireSourceDefinition) pwd.getSource();
        URI uri = ContributionResolver.getContribution(interfaze);
        pwd.getTarget().setClassLoaderId(uri);
        source.setClassLoaderId(uri);

        connector.connect(pwd);
        return interfaze.cast(source.getProxy());
    }

    private <T> LogicalWire createWire(Class<T> interfaze) throws Fabric3Exception {
        JavaServiceContract contract = introspector.introspect(interfaze);

        LogicalReference logicalReference = createReference(contract);

        LogicalCompositeComponent domainComponent = lcm.getRootComponent();

        List<LogicalService> services = autowireResolver.resolve(logicalReference, contract, domainComponent);
        if (services.isEmpty()) {
            throw new NotFoundException("Service not found for type: " + interfaze.getName());
        }
        LogicalService targetService = services.get(0);

        return new LogicalWire(domainComponent, logicalReference, targetService, SYNTHETIC_DEPLOYABLE);
    }

    private LogicalReference createReference(JavaServiceContract contract) {
        LogicalCompositeComponent domainComponent = lcm.getRootComponent();

        int id = idCounter.getAndIncrement();
        String name = "Synthetic" + id;
        URI componentUri = URI.create(domainComponent.getUri().toString() + "/" + name);
        URI referenceUri = URI.create(componentUri.toString() + "#reference");
        QName qName = new QName(HostNamespaces.SYNTHESIZED, "SyntheticComposite" + id);
        Composite composite = new Composite(qName);

        Component<NonManagedImplementation> component = new Component<>(name);
        component.setParent(composite);
        NonManagedImplementation implementation = new NonManagedImplementation();
        component.setImplementation(implementation);
        Reference<ComponentType> reference = new Reference<>("reference", Multiplicity.ONE_ONE);
        composite.add(reference);

        LogicalComponent<NonManagedImplementation> logicalComponent = new LogicalComponent<>(componentUri, component, domainComponent);
        logicalComponent.setZone(info.getZoneName());
        reference.setServiceContract(contract);
        LogicalReference logicalReference = new LogicalReference(referenceUri, reference, logicalComponent);
        logicalReference.setServiceContract(contract);

        logicalComponent.addReference(logicalReference);
        return logicalReference;
    }

}
