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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * Loader that handles &lt;include&gt; elements.
 */
public class IncludeLoader extends AbstractExtensibleTypeLoader<Include> {
    private static final QName INCLUDE = new QName(Constants.SCA_NS, "include");
    private MetaDataStore store;

    /**
     * Constructor used during bootstrap.
     *
     * @param registry the loader registry
     */
    public IncludeLoader(LoaderRegistry registry) {
        this(registry, null);
    }

    /**
     * Constructor.
     *
     * @param registry the loader registry
     * @param store    optional MetaDataStore used to resolve resources reference to by their symbolic name
     */
    @Constructor
    public IncludeLoader(@Reference LoaderRegistry registry, @Reference(required = false) MetaDataStore store) {
        super(registry);
        this.store = store;
        addAttributes("name", "scdlResource", "requires");
    }

    public QName getXMLType() {
        return INCLUDE;
    }

    public Include load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();


        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }
        QName name = LoaderUtil.getQName(nameAttr, context.getTargetNamespace(), reader.getNamespaceContext());
        String scdlResource = reader.getAttributeValue(null, "scdlResource");

        Include include = new Include();
        validateAttributes(reader, context, include);

        LoaderUtil.skipToEndElement(reader);

        ClassLoader cl = context.getClassLoader();
        URI contributionUri = context.getContributionUri();
        URL url;
        if (scdlResource != null) {
            url = cl.getResource(scdlResource);
            if (url == null) {
                include.setName(name);
                MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, startLocation, include);
                context.addError(failure);
                return include;
            }
            loadFromSideFile(include, name, cl, contributionUri, url, reader, context);
            return include;
        } else {
            if (store == null) {
                // throw error as this is invalid in a bootstrap environment
                throw new UnsupportedOperationException("scdlLocation or scdlResource must be supplied as no MetaDataStore is available");
            }

            try {
                QNameSymbol symbol = new QNameSymbol(name);
                include.setName(name);

                ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol, context);
                if (element == null) {
                    String id = name.toString();
                    MissingComposite failure = new MissingComposite("Composite not found: " + id, startLocation, include);
                    context.addError(failure);
                    // add pointer
                    URI uri = context.getContributionUri();
                    Composite pointer = new Composite(name, true, uri);
                    include.setIncluded(pointer);
                    return include;
                }
                Composite composite = element.getValue();
                include.setIncluded(composite);

                return include;
            } catch (StoreException e) {
                ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, startLocation);
                context.addError(failure);
                return include;
            }
        }
    }

    private void loadFromSideFile(Include include,
                                  QName name,
                                  ClassLoader cl,
                                  URI contributionUri,
                                  URL url,
                                  XMLStreamReader reader,
                                  IntrospectionContext context) {
        IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, cl, url);
        Location startLocation = reader.getLocation();

        Composite composite;
        try {
            Source source = new UrlSource(url);
            composite = registry.load(source, Composite.class, childContext);
        } catch (LoaderException e) {
            InvalidInclude failure = new InvalidInclude("Error loading include: " + name, e, startLocation, include);
            context.addError(failure);
            return;
        }
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
        include.setName(name);
        include.setScdlLocation(url);
        include.setIncluded(composite);
    }

}
