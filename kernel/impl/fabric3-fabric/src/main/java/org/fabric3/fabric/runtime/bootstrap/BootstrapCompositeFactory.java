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
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import f3.ContributionServiceProvider;
import f3.FabricProvider;
import f3.JDKReflectionProvider;
import f3.JavaIntrospectionProvider;
import f3.MonitorProvider;
import f3.PojoProvider;
import f3.PolicyProvider;
import f3.SystemImplementationProvider;
import f3.ThreadPoolProvider;
import f3.TransformerProvider;
import f3.XmlIntrospectionProvider;
import org.fabric3.api.Namespaces;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.InitializationException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.contribution.archive.SyntheticDirectoryClasspathProcessor;
import org.fabric3.contribution.archive.SyntheticDirectoryContributionProcessor;
import org.fabric3.contribution.processor.SymLinkClasspathProcessor;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.fabric3.spi.introspection.validation.InvalidCompositeException;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;

/**
 * Creates the initial system composite that is deployed to the runtime domain.
 */
public class BootstrapCompositeFactory {
    private static final URL COMPOSITE_URL;

    static {
        try {
            COMPOSITE_URL = new URL("file://SystemBootComposite");
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    private BootstrapCompositeFactory() {
    }

    public static Composite createSystemComposite(Contribution contribution,
                                                  HostInfo hostInfo,
                                                  ClassLoader bootClassLoader,
                                                  ImplementationIntrospector processor) throws InitializationException {

        CompositeBuilder builder = CompositeBuilder.newBuilder(new QName(Namespaces.F3, "SystemBootComposite"));
        builder.include(ContributionServiceProvider.getComposite());
        builder.include(FabricProvider.getComposite());
        builder.include(JavaIntrospectionProvider.getComposite());
        builder.include(XmlIntrospectionProvider.getComposite());
        builder.include(JDKReflectionProvider.getComposite());
        builder.include(MonitorProvider.getComposite());
        builder.include(PojoProvider.getComposite());
        builder.include(PolicyProvider.getComposite());
        builder.include(SystemImplementationProvider.getComposite());
        builder.include(ThreadPoolProvider.getComposite());
        builder.include(SystemImplementationProvider.getComposite());
        builder.include(TransformerProvider.getComposite());

        if (!hostInfo.getDeployDirectories().isEmpty()) {
            // supports file-based deploy
            builder.component(SystemComponentDefinitionBuilder.newBuilder("SyntheticDirectoryContributionProcessor",
                                                                          SyntheticDirectoryContributionProcessor.class).build());

            builder.component(SystemComponentDefinitionBuilder.newBuilder("SyntheticDirectoryClasspathProcessor",
                                                                          SyntheticDirectoryClasspathProcessor.class).build());

            builder.component(SystemComponentDefinitionBuilder.newBuilder("SymLinkClasspathProcessor", SymLinkClasspathProcessor.class).build());

        }

        Composite composite = builder.build();

        URI contributionUri = contribution.getUri();
        IntrospectionContext context = new DefaultIntrospectionContext(contributionUri, bootClassLoader, COMPOSITE_URL);
        for (ComponentDefinition<? extends Implementation<?>> definition : composite.getComponents().values()) {
            processor.introspect((InjectingComponentType) definition.getComponentType(), context);
        }

        if (context.hasErrors()) {
            QName name = composite.getName();
            List<ValidationFailure> errors = context.getErrors();
            List<ValidationFailure> warnings = context.getWarnings();
            throw new InitializationException(new InvalidCompositeException(name, errors, warnings));
        }

        addContributionUri(contributionUri, composite);
        addResource(contribution, composite);
        return composite;
    }

    /**
     * Adds the contribution URI to the system composite and its children.
     *
     * @param contributionUri the contribution URI
     * @param composite       the composite
     */
    private static void addContributionUri(URI contributionUri, Composite composite) {
        composite.setContributionUri(contributionUri);
        for (ComponentDefinition<?> definition : composite.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionUri(contributionUri, componentType);
            } else {
                definition.setContributionUri(contributionUri);
            }
        }
    }

    /**
     * Adds the composite as a resource to the contribution.
     *
     * @param contribution the contribution
     * @param composite    the composite
     */
    private static void addResource(Contribution contribution, Composite composite) {
        Source source = new UrlSource(COMPOSITE_URL);
        Resource resource = new Resource(contribution, source, Constants.COMPOSITE_CONTENT_TYPE);
        QName compositeName = composite.getName();
        QNameSymbol symbol = new QNameSymbol(compositeName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, composite);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
    }

}