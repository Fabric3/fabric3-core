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

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loader that handles &lt;include&gt; elements.
 */
public class IncludeLoader extends AbstractExtensibleTypeLoader<Include> {
    private static final QName INCLUDE = new QName(Constants.SCA_NS, "include");
    private MetaDataStore store;

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
        addAttributes("name");
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

        Include include = new Include();
        validateAttributes(reader, context, include);

        LoaderUtil.skipToEndElement(reader);

        URI contributionUri = context.getContributionUri();

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
        } catch (ContainerException e) {
            ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, startLocation);
            context.addError(failure);
            return include;
        }

    }

}
