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

import org.fabric3.host.Names;
import org.fabric3.host.Namespaces;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.failure.ValidationFailure;
import org.fabric3.api.model.type.builder.ComponentDefinitionBuilder;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.ServiceDefinition;
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

    public <T> void deploy(Class<T> interfaze, T instance) throws DeploymentException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        JavaServiceContract contract = contractProcessor.introspect(interfaze, context);

        checkErrors(context);

        // add the service contract
        ComponentDefinition<?> definition = ComponentDefinitionBuilder.newBuilder(interfaze.getSimpleName(), instance).build();
        ServiceDefinition serviceDefinition = new ServiceDefinition(interfaze.getSimpleName(), contract);
        definition.getComponentType().add(serviceDefinition);

        componentProcessor.process(definition, context);

        checkErrors(context);

        deploy(definition);
    }

    public void deploy(ComponentDefinition<?> definition) throws DeploymentException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        definition.setContributionUri(Names.HOST_CONTRIBUTION);

        componentProcessor.process(definition, context);
        checkErrors(context);

        try {
            Composite wrapper = createWrapperComposite(definition);
            wrapper.add(definition);

            domain.include(wrapper, false);
        } catch (org.fabric3.host.domain.DeploymentException e) {
            throw new DeploymentException(e);
        }
    }

    public <T> void undeploy(Class<T> interfaze, T instance) throws DeploymentException {
        undeploy(interfaze.getSimpleName());
    }

    public void undeploy(String name) throws DeploymentException {
        try {

            // find the wrapper composite used to deploy it and remove it from the host contribution
            QName compositeName = new QName(Namespaces.SYNTHESIZED, name);

            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = metaDataStore.find(Composite.class, symbol);
            if (element == null) {
                throw new DeploymentException("Component not deployed: " + name);
            }
            Composite composite = element.getValue();
            domain.undeploy(composite, false);

            Resource resource = element.getResource();
            Contribution contribution = resource.getContribution();
            contribution.getResources().remove(resource);

        } catch (org.fabric3.host.domain.DeploymentException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Creates a wrapper composite used to deploy the component to the domain. Also registers the wrapper with the Host contribution.
     *
     * @param definition the component definition to deploy
     * @return the wrapping composite
     */
    private Composite createWrapperComposite(ComponentDefinition<?> definition) {
        QName compositeName = new QName(Namespaces.SYNTHESIZED, definition.getName());
        Composite wrapper = new Composite(compositeName);
        wrapper.setContributionUri(Names.HOST_CONTRIBUTION);
        Contribution contribution = metaDataStore.find(Names.HOST_CONTRIBUTION);
        Resource resource = new Resource(contribution, null, "text/vnd.fabric3.composite+xml");
        QNameSymbol symbol = new QNameSymbol(compositeName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, wrapper);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
        return wrapper;
    }

    private void checkErrors(DefaultIntrospectionContext context) throws ValidationDeploymentException {
        List<ValidationFailure> errors = context.getErrors();
        List<ValidationFailure> warnings = context.getErrors();

        if (context.hasErrors()) {
            throw new ValidationDeploymentException(errors, warnings);
        }
    }

}
