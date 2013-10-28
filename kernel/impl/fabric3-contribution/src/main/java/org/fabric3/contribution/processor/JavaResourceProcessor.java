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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.processor.ComponentProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes a Java component resource in two phases. During contribution indexing, the Java class is introspected. At contribution processing, the component
 * resource is added to its containing composite or a new composite is created if one is not found.
 * <p/>
 * Note adding the component to the composite must be done after the contribution is indexed so that composites loaded from XML and model providers are in
 * place.
 */
@EagerInit
public class JavaResourceProcessor implements ResourceProcessor {

    private ComponentProcessor componentProcessor;

    public JavaResourceProcessor(@Reference ProcessorRegistry registry, @Reference ComponentProcessor componentProcessor) {
        this.componentProcessor = componentProcessor;
        registry.register(this);
    }

    public String getContentType() {
        return Constants.JAVA_COMPONENT_CONTENT_TYPE;
    }

    public void index(Resource resource, IntrospectionContext context) throws InstallException {
        // create component definition
        ResourceElement<?, ?> resourceElement = resource.getResourceElements().get(0);
        Class<?> clazz = (Class<?>) resourceElement.getValue();
        Component annotation = clazz.getAnnotation(Component.class);
        String name = clazz.getName();
        if (annotation.name().length() > 0) {
            name = annotation.name();
        }

        try {
            QName compositeName = QName.valueOf(annotation.composite());
            ComponentDefinition definition = new ComponentDefinition(name);

            componentProcessor.process(definition, clazz, context);

            ParsedComponentSymbol symbol = new ParsedComponentSymbol(definition);
            ResourceElement<ParsedComponentSymbol, QName> parsedElement = new ResourceElement<ParsedComponentSymbol, QName>(symbol, compositeName);
            resource.addResourceElement(parsedElement);
        } catch (IllegalArgumentException e) {
            InvalidComponentAnnotation error = new InvalidComponentAnnotation("Invalid composite name: " + name, e);
            context.addError(error);
        }
    }

    @SuppressWarnings("unchecked")
    public void process(Resource resource, IntrospectionContext context) throws InstallException {
        ResourceElement<ParsedComponentSymbol, QName> resourceElement = null;
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof ParsedComponentSymbol) {
                resourceElement = (ResourceElement<ParsedComponentSymbol, QName>) element;
                break;
            }
        }
        ComponentDefinition<?> definition = resourceElement.getSymbol().getKey();
        QName compositeName = resourceElement.getValue();
        QNameSymbol compositeSymbol = new QNameSymbol(compositeName);
        Contribution contribution = resource.getContribution();

        Composite composite = null;
        for (Resource contributionResource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : contributionResource.getResourceElements()) {
                if (element.getSymbol().equals(compositeSymbol)) {
                    composite = (Composite) element.getValue();
                    break;
                }
            }
        }
        if (composite == null) {
            composite = new Composite(compositeName);
            composite.setContributionUri(contribution.getUri());
            NullSource source = new NullSource(compositeName.toString());
            Resource compositeResource = new Resource(contribution, source, Constants.COMPOSITE_CONTENT_TYPE);
            compositeResource.setState(ResourceState.PROCESSED);
            compositeResource.addResourceElement(new ResourceElement<QNameSymbol, Composite>(compositeSymbol, composite));
            resource.setContribution(contribution);
            contribution.addResource(compositeResource);

            ContributionManifest manifest = contribution.getManifest();
            if (manifest.getDeployables().isEmpty()) {
                Deployable deployable = new Deployable(compositeName);
                manifest.addDeployable(deployable);
            }
        }
        composite.add(definition);

        resource.setState(ResourceState.PROCESSED);
    }

    private class ParsedComponentSymbol extends Symbol<ComponentDefinition> {

        public ParsedComponentSymbol(ComponentDefinition definition) {
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
