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
package org.fabric3.node.domain;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.api.host.HostNamespaces;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.builder.JavaComponentDefinitionBuilder;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.processor.ComponentProcessor;
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

    public void deploy(String name, Object instance, Class<?>... interfaces) throws DeploymentException {
        ComponentDefinition<?> definition = JavaComponentDefinitionBuilder.newBuilder(name, instance).build();
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

    public void deploy(Composite composite) throws DeploymentException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(Names.HOST_CONTRIBUTION, getClass().getClassLoader());

        // enrich the model
        for (ComponentDefinition<? extends Implementation<?>> definition : composite.getComponents().values()) {
            componentProcessor.process(definition, context);
        }
        checkErrors(context);

        // validate model

        setContributionUris(composite);

        try {
            addCompositeToContribution(composite);
            domain.include(composite, false);
        } catch (org.fabric3.api.host.domain.DeploymentException e) {
            // TODO remove the contribution
            throw new DeploymentException(e);
        }
    }

    public void deploy(ComponentDefinition<?> definition) throws DeploymentException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(Names.HOST_CONTRIBUTION, getClass().getClassLoader());
        definition.setContributionUri(Names.HOST_CONTRIBUTION);

        componentProcessor.process(definition, context);
        checkErrors(context);

        try {
            Composite wrapper = createWrapperComposite(definition.getName());
            wrapper.add(definition);

            domain.include(wrapper, false);
        } catch (org.fabric3.api.host.domain.DeploymentException e) {
            throw new DeploymentException(e);
        }
    }

    public void deploy(ChannelDefinition definition) throws DeploymentException {
        try {
            definition.setContributionUri(Names.HOST_CONTRIBUTION);
            Composite wrapper = createWrapperComposite(definition.getName());
            wrapper.add(definition);
            domain.include(wrapper, false);
        } catch (org.fabric3.api.host.domain.DeploymentException e) {
            throw new DeploymentException(e);
        }
    }

    public void undeploy(QName name) throws DeploymentException {
        try {

            QNameSymbol symbol = new QNameSymbol(name);
            ResourceElement<QNameSymbol, Composite> element = metaDataStore.find(Composite.class, symbol);
            if (element == null) {
                throw new DeploymentException("Component not deployed: " + name);
            }
            Composite composite = element.getValue();
            domain.undeploy(composite, false);

            Resource resource = element.getResource();
            Contribution contribution = resource.getContribution();
            contribution.getResources().remove(resource);

        } catch (org.fabric3.api.host.domain.DeploymentException e) {
            throw new DeploymentException(e);
        }
    }

    public void undeploy(String name) throws DeploymentException {
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
        wrapper.setContributionUri(Names.HOST_CONTRIBUTION);

        addCompositeToContribution(wrapper);

        return wrapper;
    }

    private void addCompositeToContribution(Composite wrapper) {
        QName compositeName = wrapper.getName();
        Contribution contribution = metaDataStore.find(Names.HOST_CONTRIBUTION);
        Resource resource = new Resource(contribution, null, Constants.COMPOSITE_CONTENT_TYPE);
        QNameSymbol symbol = new QNameSymbol(compositeName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, wrapper);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
    }

    private void addService(Class<?> interfaze, ComponentDefinition<?> definition) throws ValidationDeploymentException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext(Names.HOST_CONTRIBUTION, getClass().getClassLoader());
        JavaServiceContract contract = contractProcessor.introspect(interfaze, context);
        ServiceDefinition serviceDefinition = new ServiceDefinition(interfaze.getSimpleName(), contract);
        definition.getComponentType().add(serviceDefinition);
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
        composite.setContributionUri(Names.HOST_CONTRIBUTION);
        for (ComponentDefinition<? extends Implementation<?>> definition : composite.getComponents().values()) {
            definition.setContributionUri(Names.HOST_CONTRIBUTION);
            if (definition.getComponentType() instanceof Composite) {
                setContributionUris((Composite) definition.getComponentType());
            }
        }
        for (ChannelDefinition definition : composite.getChannels().values()) {
            definition.setContributionUri(Names.HOST_CONTRIBUTION);
        }
    }

}
