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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.JavaResourceProcessorExtension;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ComponentProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes a Java component resource in two phases. During contribution indexing, the Java class is introspected. At contribution processing, the component
 * resource is added to its containing composite or a new composite is created if one is not found. <p/> Note adding the component to the composite must be done
 * after the contribution is indexed so that composites loaded from XML and model providers are in place.
 */
@EagerInit
public class JavaResourceProcessor implements ResourceProcessor {

    private ComponentProcessor componentProcessor;
    private MetaDataStore store;
    private List<JavaResourceProcessorExtension> processorExtensions = Collections.emptyList();

    public JavaResourceProcessor(@Reference ProcessorRegistry registry, @Reference ComponentProcessor componentProcessor, @Reference MetaDataStore store) {
        this.componentProcessor = componentProcessor;
        this.store = store;
        registry.register(this);
    }

    @Reference(required = false)
    public void setProcessorExtensions(List<JavaResourceProcessorExtension> processorExtensions) {
        this.processorExtensions = processorExtensions;
    }

    public String getContentType() {
        return Constants.JAVA_COMPONENT_CONTENT_TYPE;
    }

    public void index(Resource resource, IntrospectionContext context) throws Fabric3Exception {
        // create component definition
        ResourceElement<?, ?> resourceElement = resource.getResourceElements().get(0);
        Class<?> clazz = (Class<?>) resourceElement.getValue();

        org.fabric3.api.annotation.model.Component annotation = clazz.getAnnotation(org.fabric3.api.annotation.model.Component.class);
        String name = clazz.getSimpleName();
        if (annotation != null && annotation.name().length() > 0) {
            name = annotation.name();
        }

        try {
            QName compositeName = getCompositeName(resourceElement, annotation);

            Component definition = new Component(name);
            definition.setContributionUri(context.getContributionUri());
            componentProcessor.process(definition, clazz, context);

            ParsedComponentSymbol symbol = new ParsedComponentSymbol(definition);
            ResourceElement<ParsedComponentSymbol, QName> parsedElement = new ResourceElement<>(symbol, compositeName);
            resource.addResourceElement(parsedElement);
        } catch (IllegalArgumentException e) {
            InvalidComponentAnnotation error = new InvalidComponentAnnotation("Invalid composite name: " + name + " on class: " + clazz.getName(), e);
            context.addError(error);
        }
    }

    private QName getCompositeName(ResourceElement<?, ?> resourceElement, org.fabric3.api.annotation.model.Component annotation) {
        QName compositeName;
        if (annotation != null) {
            compositeName = QName.valueOf(annotation.composite());
        } else {
            compositeName = resourceElement.getMetadata(QName.class);
            if (compositeName == null) {
                compositeName = QName.valueOf(org.fabric3.api.annotation.model.Component.DEFAULT_COMPOSITE);
            }
        }
        return compositeName;
    }

    @SuppressWarnings("unchecked")
    public void process(Resource resource, IntrospectionContext context) throws Fabric3Exception {
        ResourceElement<ParsedComponentSymbol, QName> resourceElement = null;
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof ParsedComponentSymbol) {
                resourceElement = (ResourceElement<ParsedComponentSymbol, QName>) element;
                break;
            }
        }
        Component<JavaImplementation> definition = resourceElement.getSymbol().getKey();
        QName compositeName = resourceElement.getValue();
        QNameSymbol compositeSymbol = new QNameSymbol(compositeName);
        Contribution contribution = resource.getContribution();

        Composite composite = null;
        ResourceElement<QNameSymbol, Composite> element = store.resolve(context.getContributionUri(), Composite.class, compositeSymbol, context);
        if (element != null) {
            composite = element.getValue();
        }
        if (composite == null) {
            composite = new Composite(compositeName);
            composite.setContributionUri(contribution.getUri());
            composite.setDeployable(true); // make the composite deployable if it has not been defined in either a composite file or via the DSL
            NullSource source = new NullSource(compositeName.toString());
            Resource compositeResource = new Resource(contribution, source, Constants.COMPOSITE_CONTENT_TYPE);
            compositeResource.setState(ResourceState.PROCESSED);
            compositeResource.addResourceElement(new ResourceElement<>(compositeSymbol, composite));
            resource.setContribution(contribution);
            contribution.addResource(compositeResource);

            ContributionManifest manifest = contribution.getManifest();
            if (manifest.getDeployables().isEmpty()) {
                Deployable deployable = new Deployable(compositeName);
                manifest.addDeployable(deployable);
            }
            composite.add(definition);
        } else {
            composite.add(definition);
            updateIncludingComposites(contribution, composite);
        }

        for (JavaResourceProcessorExtension extension : processorExtensions) {
            extension.process(definition);
        }

        resource.setState(ResourceState.PROCESSED);
    }

    /**
     * Updates composites that include the given composite after a component has been added to the later. This is necessary since the including composite caches
     * a view of the included components. <p/> Note only the current contribution needs to be searched as contributions are ordered on install.
     *
     * @param contribution the current contribution
     * @param composite    the composite
     */
    private void updateIncludingComposites(Contribution contribution, Composite composite) {
        for (Resource contributionResource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : contributionResource.getResourceElements()) {
                if (element.getValue() instanceof Composite) {
                    Composite candidate = (Composite) element.getValue();
                    for (Include include : candidate.getIncludes().values()) {
                        if (include.getIncluded().getName().equals(composite.getName())) {
                            candidate.add(include);
                            break;
                        }
                    }
                }
            }
        }
    }

    private class ParsedComponentSymbol extends Symbol<Component> {

        public ParsedComponentSymbol(Component definition) {
            super(definition);
        }
    }

    private class NullSource implements Source {
        private String name;

        private NullSource(String name) {
            this.name = name;
        }

        public String getSystemId() {
            return name;
        }

        public URL getBaseLocation() {
            return null;
        }

        public InputStream openStream() throws IOException {
            return null;
        }

        public Source getImportSource(String parentLocation, String importLocation) throws IOException {
            return null;
        }
    }

}
