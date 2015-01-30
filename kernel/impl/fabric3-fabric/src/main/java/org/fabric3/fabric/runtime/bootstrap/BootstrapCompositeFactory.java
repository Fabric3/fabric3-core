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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
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
import org.fabric3.spi.model.type.system.SystemComponentBuilder;

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
        builder.include(SystemImplementationProvider.getComposite());
        builder.include(ThreadPoolProvider.getComposite());
        builder.include(SystemImplementationProvider.getComposite());
        builder.include(TransformerProvider.getComposite());

        if (!hostInfo.getDeployDirectories().isEmpty()) {
            // supports file-based deploy
            builder.component(SystemComponentBuilder.newBuilder("SyntheticDirectoryContributionProcessor",
                                                                SyntheticDirectoryContributionProcessor.class).build());

            builder.component(SystemComponentBuilder.newBuilder("SyntheticDirectoryClasspathProcessor", SyntheticDirectoryClasspathProcessor.class).build());

            builder.component(SystemComponentBuilder.newBuilder("SymLinkClasspathProcessor", SymLinkClasspathProcessor.class).build());

        }

        Composite composite = builder.build();

        URI contributionUri = contribution.getUri();
        IntrospectionContext context = new DefaultIntrospectionContext(contributionUri, bootClassLoader, COMPOSITE_URL);
        for (Component<? extends Implementation<?>> definition : composite.getComponents().values()) {
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
        for (Component<?> definition : composite.getComponents().values()) {
            if (definition.getComponentType() instanceof Composite) {
                Composite componentType = Composite.class.cast(definition.getComponentType());
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
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol, composite);
        resource.addResourceElement(element);
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);
    }

}