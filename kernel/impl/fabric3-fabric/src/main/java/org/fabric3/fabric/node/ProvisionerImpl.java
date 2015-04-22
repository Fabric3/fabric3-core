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
package org.fabric3.fabric.node;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.HostNamespaces;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.builder.JavaComponentBuilder;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.java.ComponentProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ProvisionerImpl implements Provisioner {
    private JavaContractProcessor contractProcessor;
    private ComponentProcessor componentProcessor;
    private MetaDataStore metaDataStore;
    private Domain domain;

    public ProvisionerImpl(@Reference JavaContractProcessor contractProcessor,
                           @Reference ComponentProcessor componentProcessor,
                           @Reference MetaDataStore metaDataStore,
                           @Reference(name = "domain") Domain domain) {
        this.contractProcessor = contractProcessor;
        this.componentProcessor = componentProcessor;
        this.metaDataStore = metaDataStore;
        this.domain = domain;
    }

    public void deploy(String name, Object instance, Class<?>... interfaces) throws Fabric3Exception {
        Component<?> definition = JavaComponentBuilder.newBuilder(name, instance).build();
        if (interfaces == null) {
            // if no interfaces are specified, check if the implementation class implements one or more interfaces
            Class<?>[] implementedInterfaces = getClass().getInterfaces();
            if (implementedInterfaces.length == 0) {
                // use the implementation class as the service interface
                addService(instance.getClass(), definition);
            } else {
                // use all of the implemented interfaces as service interfaces
                for (Class<?> interfaze : implementedInterfaces) {
                    addService(interfaze, definition);
                }
            }
        } else {
            for (Class<?> interfaze : interfaces) {
                addService(interfaze, definition);
            }
        }

        deploy(definition);
    }

    public void deploy(Composite composite) throws Fabric3Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(ContributionResolver.getContribution(), getClass().getClassLoader());

        // enrich the model
        for (Component<? extends Implementation<?>> definition : composite.getComponents().values()) {
            componentProcessor.process(definition, context);
        }
        checkErrors(context);

        // validate model

        setContributionUris(composite);
        addCompositeToContribution(composite);
        domain.include(composite);
    }

    public void deploy(Component<?> component) throws Fabric3Exception {
        URI uri = ContributionResolver.getContribution();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(uri, getClass().getClassLoader());
        component.setContributionUri(uri);

        componentProcessor.process(component, context);
        checkErrors(context);

        Composite wrapper = createWrapperComposite(component.getName());
        wrapper.add(component);

        domain.include(wrapper);
    }

    public void deploy(Channel channel) throws Fabric3Exception {
        Composite wrapper = createWrapperComposite(channel.getName());
        wrapper.add(channel);
        domain.include(wrapper);
    }

    public void undeploy(QName name) throws Fabric3Exception {
        QNameSymbol symbol = new QNameSymbol(name);
        ResourceElement<QNameSymbol, Composite> element = metaDataStore.find(Composite.class, symbol);
        if (element == null) {
            throw new Fabric3Exception("Component not deployed: " + name);
        }
        Composite composite = element.getValue();
        domain.undeploy(composite);

        Resource resource = element.getResource();
        Contribution contribution = resource.getContribution();
        contribution.getResources().remove(resource);

    }

    public void undeploy(String name) throws Fabric3Exception {
        // find the wrapper composite used to deploy it and remove it from the host contribution
        QName compositeName = new QName(HostNamespaces.SYNTHESIZED, name);
        undeploy(compositeName);
    }

    /**
     * Creates a wrapper composite used to deploy the component to the domain. Also registers the wrapper with the Host contribution.
     *
     * @param name the composite name
     * @return the wrapping composite
     */
    private Composite createWrapperComposite(String name) {
        QName compositeName = new QName(HostNamespaces.SYNTHESIZED, name);
        Composite wrapper = new Composite(compositeName);
        URI uri = ContributionResolver.getContribution();
        wrapper.setContributionUri(uri);

        addCompositeToContribution(wrapper);

        return wrapper;
    }

    private void addCompositeToContribution(Composite wrapper) {
        QName compositeName = wrapper.getName();
        URI uri = ContributionResolver.getContribution();
        Contribution contribution = metaDataStore.find(uri);
        Resource resource = new Resource(contribution, null, Constants.COMPOSITE_CONTENT_TYPE);
        QNameSymbol symbol = new QNameSymbol(compositeName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, wrapper);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
    }

    private void addService(Class<?> interfaze, Component<?> definition) throws ValidationDeploymentException {
        URI uri = ContributionResolver.getContribution();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(uri, getClass().getClassLoader());
        JavaServiceContract contract = contractProcessor.introspect(interfaze, context);
        Service<ComponentType> service = new Service<>(interfaze.getSimpleName(), contract);
        definition.getComponentType().add(service);
        checkErrors(context);
    }

    private void checkErrors(DefaultIntrospectionContext context) throws ValidationDeploymentException {
        List<ValidationFailure> errors = context.getErrors();
        List<ValidationFailure> warnings = context.getErrors();
        if (context.hasErrors()) {
            throw new ValidationDeploymentException(errors, warnings);
        }
    }

    private void setContributionUris(Composite composite) {
        URI uri = ContributionResolver.getContribution();
        composite.setContributionUri(uri);
        for (Component<? extends Implementation<?>> definition : composite.getComponents().values()) {
            definition.setContributionUri(uri);
            if (definition.getComponentType() instanceof Composite) {
                setContributionUris((Composite) definition.getComponentType());
            }
        }
    }

}
