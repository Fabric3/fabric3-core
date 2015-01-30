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
package org.fabric3.implementation.web.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.implementation.web.model.WebComponentType;
import org.fabric3.implementation.web.model.WebImplementation;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;

/**
 * Loads <code><implementation.web></code> from a composite.
 */
@EagerInit
public class WebComponentLoader extends AbstractValidatingTypeLoader<WebImplementation> {
    private static final QName IMPLEMENTATION_WEB = new QName(org.oasisopen.sca.Constants.SCA_NS, "implementation.web");
    private LoaderRegistry registry;
    private MetaDataStore metaDataStore;

    public WebComponentLoader(@org.oasisopen.sca.annotation.Reference LoaderRegistry registry, @org.oasisopen.sca.annotation.Reference MetaDataStore metaDataStore) {
        this.registry = registry;
        this.metaDataStore = metaDataStore;
        addAttributes("uri");
    }

    @Init
    public void init() {
        registry.registerLoader(IMPLEMENTATION_WEB, this);
    }

    public WebImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        Contribution contribution = metaDataStore.find(context.getContributionUri());

        URI uri = parseUri(reader, startLocation, context);
        WebImplementation implementation = new WebImplementation(uri);
        validateAttributes(reader, context, implementation);

        try {
            // find the component type created during indexing of Java artifacts (or create one if necessary)
            WebComponentType type = getComponentType(contribution);
            implementation.setComponentType(type);

            // check if an explicit component type file is present (required for backward compatibility)
            ComponentType componentType = loadComponentType(context);
            for (Map.Entry<String, Reference<ComponentType>> entry : componentType.getReferences().entrySet()) {
                type.add(entry.getValue());
            }
            for (Map.Entry<String, Property> entry : componentType.getProperties().entrySet()) {
                type.add(entry.getValue());
            }
        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                // ignore since we allow component types not to be specified in the web app 
            } else {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading web.componentType", e, startLocation);
                context.addError(failure);
                return null;
            }
        }
        LoaderUtil.skipToEndElement(reader);

        // add an index entry so it can be determined that a web implementation does not need to be synthesized (it was explicitly created by the contribution)
        IndexHelper.indexImplementation(implementation, contribution);

        return implementation;
    }

    private URI parseUri(XMLStreamReader reader, Location location, IntrospectionContext context) {
        URI uri = null;
        String uriStr = reader.getAttributeValue(null, "uri");
        if (uriStr != null) {
            if (uriStr.length() < 1) {
                InvalidValue failure = new InvalidValue("Web component URI must specify a value", location);
                context.addError(failure);
            } else {
                try {
                    uri = new URI(uriStr);
                } catch (URISyntaxException e) {
                    InvalidValue failure = new InvalidValue("Web component URI is not a valid: " + uri, location);
                    context.addError(failure);
                }
            }
        }
        return uri;
    }

    private ComponentType loadComponentType(IntrospectionContext context) throws LoaderException {
        URL url;
        try {
            url = new URL(context.getSourceBase(), "web.componentType");
        } catch (MalformedURLException e) {
            // this should not happen
            throw new AssertionError(e);
        }
        Source source = new UrlSource(url);
        IntrospectionContext childContext = new DefaultIntrospectionContext(null, context.getClassLoader(), url);
        ComponentType componentType = registry.load(source, ComponentType.class, childContext);
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        return componentType;
    }

    /**
     * Returns the web component type created during index of Java artifacts or creates one if necessary (i.e. no artifacts generated component type metadata).
     *
     * @param contribution the current contribution
     * @return the web component type
     */
    private WebComponentType getComponentType(Contribution contribution) {
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol() instanceof WebComponentTypeSymbol) {
                    return (WebComponentType) element.getValue();
                }
            }
        }
        return new WebComponentType();
    }

}
