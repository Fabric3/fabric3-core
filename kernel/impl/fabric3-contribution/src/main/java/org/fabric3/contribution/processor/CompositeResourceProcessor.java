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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects a composite file in a contribution and produces a Composite type. This implementation assumes the CCL has all necessary artifacts to perform
 * introspection on its classpath.
 */
@EagerInit
public class CompositeResourceProcessor implements ResourceProcessor {
    private Loader loader;
    private final XMLInputFactory xmlFactory;

    public CompositeResourceProcessor(@Reference ProcessorRegistry processorRegistry, @Reference Loader loader) {
        processorRegistry.register(this);
        this.loader = loader;
        this.xmlFactory = XMLInputFactory.newFactory();
    }

    public String getContentType() {
        return Constants.COMPOSITE_CONTENT_TYPE;
    }

    public void index(Resource resource, IntrospectionContext context) throws Fabric3Exception {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            Source source = resource.getSource();
            stream = source.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            Location startLocation = reader.getLocation();
            if (!"composite".equals(reader.getName().getLocalPart())) {
                // not a composite root element
                return;
            }
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                context.addError(new MissingAttribute("Composite name not specified", startLocation));
                return;
            }
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            QName compositeName = new QName(targetNamespace, name);
            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<>(symbol);
            resource.addResourceElement(element);
            validateUnique(resource, element, reader, context);
        } catch (XMLStreamException | IOException e) {
            throw new Fabric3Exception(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void process(Resource resource, IntrospectionContext context) throws Fabric3Exception {
        Source source = resource.getSource();
        ClassLoader classLoader = context.getClassLoader();
        URI contributionUri = context.getContributionUri();
        URL location = source.getBaseLocation();
        IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, classLoader, location);
        Composite composite;
        try {
            // check to see if the resource has already been evaluated
            composite = loader.load(source, Composite.class, childContext);
        } catch (LoaderException e) {
            throw new Fabric3Exception(e);
        }
        if (composite == null) {
            // composite could not be parsed
            InvalidXmlArtifact error = new InvalidXmlArtifact("Invalid composite: " + location, null);
            context.addError(error);
            return;
        }
        boolean found = false;
        for (ResourceElement element : resource.getResourceElements()) {
            if (element.getSymbol().getKey().equals(composite.getName())) {
                element.setValue(composite);
                found = true;
                break;
            }
        }
        if (!found) {
            // should not happen
            String identifier = composite.getName().toString();
            throw new AssertionError("Resource element not found: " + identifier);
        }
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        resource.setState(ResourceState.PROCESSED);

    }

    private void validateUnique(Resource resource, ResourceElement<QNameSymbol, Composite> element, XMLStreamReader reader, IntrospectionContext context) {
        Contribution contribution = resource.getContribution();
        for (Resource entry : contribution.getResources()) {
            if (resource == entry) {
                // skip self since the resource is added to the contribution and will be iterated
                continue;
            }
            if (resource.getContentType().equals(entry.getContentType())) {
                for (ResourceElement<?, ?> elementEntry : entry.getResourceElements()) {
                    if (element.getSymbol().equals(elementEntry.getSymbol())) {
                        QName name = element.getSymbol().getKey();
                        Location location = reader.getLocation();
                        Composite composite = element.getValue();
                        DuplicateComposite error = new DuplicateComposite("Duplicate composite found with name: " + name, location, composite);
                        context.addError(error);
                        break;
                    }
                }
            }
        }
    }

}
