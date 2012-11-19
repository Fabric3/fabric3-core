/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
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
 * Loader that handles an &lt;implementation.composite&gt; element.
 */
@EagerInit
public class ImplementationCompositeLoader extends AbstractExtensibleTypeLoader<CompositeImplementation> {
    private static final QName IMPL = new QName(Constants.SCA_NS, "implementation.composite");

    private MetaDataStore store;

    public ImplementationCompositeLoader(@Reference LoaderRegistry registry, @Reference MetaDataStore store) {
        super(registry);
        addAttributes("name", "scdlResource", "requires");
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
