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
package org.fabric3.introspection.xml.definitions;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.Set;

import org.fabric3.api.model.type.definitions.ImplementationType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loader for definitions.
 */
@EagerInit
public class ImplementationTypeLoader extends AbstractValidatingTypeLoader<ImplementationType> {
    private static final QName QNAME = new QName(Constants.SCA_NS, "implementationType");

    private LoaderRegistry registry;
    private LoaderHelper helper;

    public ImplementationTypeLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper helper) {
        this.registry = registry;
        this.helper = helper;
        addAttributes("name", "alwaysProvides", "mayProvide", "type");
    }

    @Init
    public void init() {
        registry.registerLoader(QNAME, this);
    }

    public ImplementationType load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        try {
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                // support old SCA and SCA 1.1 attributes for backward compatibility
                name = reader.getAttributeValue(null, "type");
                if (name == null) {
                    MissingAttribute error = new MissingAttribute("Implementation type name not specified", startLocation);
                    context.addError(error);
                    return null;
                }
            }

            NamespaceContext namespaceContext = reader.getNamespaceContext();
            QName qName = LoaderUtil.getQName(name, context.getTargetNamespace(), namespaceContext);

            if (!org.fabric3.api.Namespaces.F3.equals(qName.getNamespaceURI()) && !registry.isRegistered(qName)) {
                // do not check F3 namespaces as definitions files may be contributed during bootstrap when the F3 implementation loaders have not yet been
                // registered
                InvalidValue error = new InvalidValue("Unknown implementation type: " + qName, startLocation);
                context.addError(error);
            } else if (!qName.getLocalPart().startsWith("implementation.")) {
                InvalidValue error = new InvalidValue("Invalid implementation value: " + qName, startLocation);
                context.addError(error);
            }
            Set<QName> alwaysProvides = helper.parseListOfQNames(reader, "alwaysProvides");
            Set<QName> mayProvide = helper.parseListOfQNames(reader, "mayProvide");
            ImplementationType implementationType = new ImplementationType(qName, alwaysProvides, mayProvide);

            validateAttributes(reader, context, implementationType);

            LoaderUtil.skipToEndElement(reader);
            return implementationType;
        } catch (InvalidPrefixException e) {
            String prefix = e.getPrefix();
            URI uri = context.getContributionUri();
            InvalidQNamePrefix failure = new InvalidQNamePrefix(
                    "The prefix " + prefix + " specified in the definitions.xml file in contribution " + uri + " is invalid", startLocation);
            context.addError(failure);

        }
        return null;
    }

}
