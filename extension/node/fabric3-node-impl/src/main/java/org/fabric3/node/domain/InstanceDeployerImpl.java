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
import java.net.URI;

import org.fabric3.host.Namespaces;
import org.fabric3.host.failure.ValidationFailure;
import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.implementation.java.runtime.JavaComponent;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Scope;
import org.fabric3.node.nonmanaged.NonManagedImplementationManagerFactory;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.cm.RegistrationException;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.generator.WireGenerator;
import org.fabric3.spi.instantiator.AutowireResolver;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class InstanceDeployerImpl implements InstanceDeployer {
    private static final QName SYNTHETIC_DEPLOYABLE = new QName(Namespaces.SYNTHESIZED, "SyntheticDeployable");
    private static final Composite SYNTHETIC_COMPOSITE = new Composite(new QName(Namespaces.SYNTHESIZED, "SyntheticComposite"));

    private JavaContractProcessor contractProcessor;
    private LogicalComponentManager lcm;
    private ComponentManager cm;
    private ScopeContainer scopeContainer;

    public InstanceDeployerImpl(@Reference JavaContractProcessor contractProcessor,
                                @Reference(name = "lcm") LogicalComponentManager lcm,
                                @Reference AutowireResolver autowireResolver,
                                @Reference WireGenerator wireGenerator,
                                @Reference ComponentManager cm,
                                @Reference ScopeRegistry scopeRegistry) {
        this.contractProcessor = contractProcessor;
        this.lcm = lcm;
        this.cm = cm;
        this.scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    public <T> void deploy(Class<T> interfaze, T instance) throws DeploymentException {
        JavaServiceContract contract = introspectInterface(interfaze);

        LogicalComponent<JavaImplementation> logicalComponent = instantiateLogicalComponent(interfaze, contract);

        buildComponent(logicalComponent, instance);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public <T> void undeploy(Class<T> interfaze, T instance) throws DeploymentException {
        LogicalCompositeComponent domainComponent = lcm.getRootComponent();
        String name = interfaze.getSimpleName();
        URI componentUri = URI.create(domainComponent.getUri().toString() + "/" + name);
        domainComponent.getComponents().remove(componentUri);
        try {
            cm.unregister(componentUri);
        } catch (RegistrationException e) {
            throw new DeploymentException(e);
        }
    }

    private <T> JavaServiceContract introspectInterface(Class<T> interfaze) throws DeploymentException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        JavaServiceContract contract = contractProcessor.introspect(interfaze, context);
        StringBuilder builder = new StringBuilder();
        if (context.hasErrors()) {
            for (ValidationFailure failure : context.getErrors()) {
                builder.append(failure.getMessage()).append("\n");
            }
            throw new DeploymentException(builder.toString());
        }
        return contract;
    }

    private <T> LogicalComponent<JavaImplementation> instantiateLogicalComponent(Class<T> interfaze, JavaServiceContract contract) {
        LogicalCompositeComponent domainComponent = lcm.getRootComponent();

        InjectingComponentType componentType = new InjectingComponentType();
        componentType.setScope("COMPOSITE");
        JavaImplementation implementation = new JavaImplementation();
        implementation.setComponentType(componentType);

        String name = interfaze.getSimpleName();
        ComponentDefinition<JavaImplementation> definition = new ComponentDefinition<JavaImplementation>(name);
        definition.setParent(SYNTHETIC_COMPOSITE);  // add deployable composite
        definition.setImplementation(implementation);

        URI componentUri = URI.create(domainComponent.getUri().toString() + "/" + name);
        URI serviceUri = URI.create(componentUri.toString() + "#" + name);
        LogicalComponent<JavaImplementation> component = new LogicalComponent<JavaImplementation>(componentUri, definition, domainComponent);
        LogicalService service = new LogicalService(serviceUri, null, component);
        service.setServiceContract(contract);
        component.addService(service);

        domainComponent.addComponent(component);
        return component;
    }

    private <T> void buildComponent(LogicalComponent<JavaImplementation> logicalComponent, T instance) throws DeploymentException {
        try {
            URI componentUri = logicalComponent.getUri();
            NonManagedImplementationManagerFactory factory = new NonManagedImplementationManagerFactory(instance);
            JavaComponent javaComponent = new JavaComponent(componentUri, factory, scopeContainer, SYNTHETIC_DEPLOYABLE, false);
            javaComponent.start();
            cm.register(javaComponent);
        } catch (RegistrationException e) {
            throw new DeploymentException(e);
        } catch (ComponentException e) {
            throw new DeploymentException(e);
        }
    }

}
