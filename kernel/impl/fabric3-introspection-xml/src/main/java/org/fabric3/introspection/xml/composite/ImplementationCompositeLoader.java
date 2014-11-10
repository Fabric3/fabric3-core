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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loader that handles an &lt;implementation.composite&gt; element.
 */
@EagerInit
public class ImplementationCompositeLoader extends AbstractExtensibleTypeLoader<CompositeImplementation> {
    private static final QName IMPL = new QName(Constants.SCA_NS, "implementation.composite");

    private MetaDataStore store;
    private LoaderHelper loaderHelper;

    public ImplementationCompositeLoader(@Reference LoaderRegistry registry, @Reference MetaDataStore store, @Reference LoaderHelper loaderHelper) {
        super(registry);
        this.loaderHelper = loaderHelper;
        addAttributes("name", "scdlResource", "requires", "policySets");
        this.store = store;
    }

    public QName getXMLType() {
        return IMPL;
    }

    public CompositeImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        // read name now b/c the reader skips ahead
        String scdlResource = reader.getAttributeValue(null, "scdlResource");

        CompositeImplementation implementation;
        if (scdlResource != null) {
            implementation = parseScdlResource(scdlResource, startLocation, context);
        } else {
            implementation = resolveByName(reader, startLocation, context);
        }

        validateAttributes(reader, context, implementation);

        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);


        LoaderUtil.skipToEndElement(reader);
        return implementation;
    }

    private CompositeImplementation parseScdlResource(String scdlResource, Location startLocation, IntrospectionContext context) {
        ClassLoader cl = context.getClassLoader();
        CompositeImplementation impl = new CompositeImplementation();
        URI contributionUri = context.getContributionUri();

        URL url = cl.getResource(scdlResource);
        if (url == null) {
            MissingComposite failure = new MissingComposite("Composite file not found: " + scdlResource, startLocation, impl);
            context.addError(failure);
            return impl;
        }
        Source source = new UrlSource(url);
        IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, cl, url);
        Composite composite;
        try {
            composite = registry.load(source, Composite.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            if (composite == null) {
                // error loading composite, return
                return null;
            }
        } catch (LoaderException e) {
            ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, startLocation);
            context.addError(failure);
            return null;
        }
        impl.setName(composite.getName());
        impl.setComponentType(composite);
        return impl;
    }

    private CompositeImplementation resolveByName(XMLStreamReader reader, Location startLocation, IntrospectionContext context) {
        String nameAttr = reader.getAttributeValue(null, "name");
        if (nameAttr == null || nameAttr.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", startLocation);
            context.addError(failure);
            return null;
        }

        CompositeImplementation impl = new CompositeImplementation();
        URI contributionUri = context.getContributionUri();

        QName name = LoaderUtil.getQName(nameAttr, context.getTargetNamespace(), reader.getNamespaceContext());

        try {
            QNameSymbol symbol = new QNameSymbol(name);
            ResourceElement<QNameSymbol, Composite> element = store.resolve(contributionUri, Composite.class, symbol, context);
            if (element == null) {
                String id = name.toString();
                MissingComposite failure = new MissingComposite("Composite not found: " + id, startLocation, impl);
                context.addError(failure);
                // add pointer
                URI uri = context.getContributionUri();
                Composite pointer = new Composite(name, true, uri);
                impl.setComponentType(pointer);
                return impl;
            }
            impl.setComponentType(element.getValue());
            return impl;
        } catch (StoreException e) {
            ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, startLocation);
            context.addError(failure);
            return null;

        }
    }

}
