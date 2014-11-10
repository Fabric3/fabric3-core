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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.Set;

import org.fabric3.api.model.type.definitions.BindingType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loader for definitions.
 */
@EagerInit
public class BindingTypeLoader extends AbstractValidatingTypeLoader<BindingType> {

    private final LoaderHelper helper;

    public BindingTypeLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
        addAttributes("name", "type", "alwaysProvides", "mayProvide");
    }

    public BindingType load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            // support old SCA and SCA 1.1 attributes for backward compatibility
            name = reader.getAttributeValue(null, "type");
        }
        QName qName = LoaderUtil.getQName(name, context.getTargetNamespace(), reader.getNamespaceContext());

        Set<QName> alwaysProvides;
        try {
            alwaysProvides = helper.parseListOfQNames(reader, "alwaysProvides");
            Set<QName> mayProvide = helper.parseListOfQNames(reader, "mayProvide");
            BindingType bindingType = new BindingType(qName, alwaysProvides, mayProvide);
            validateAttributes(reader, context, bindingType);
            LoaderUtil.skipToEndElement(reader);
            return bindingType;
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
