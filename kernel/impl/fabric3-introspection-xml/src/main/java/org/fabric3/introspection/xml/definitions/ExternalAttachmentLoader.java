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
package org.fabric3.introspection.xml.definitions;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.Set;

import org.fabric3.api.model.type.definitions.ExternalAttachment;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loader for the externalAttachment element.
 */
@EagerInit
public class ExternalAttachmentLoader extends AbstractValidatingTypeLoader<ExternalAttachment> {

    private final LoaderHelper helper;

    public ExternalAttachmentLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
        addAttributes("intents", "policySets", "attachTo");
    }

    public ExternalAttachment load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        try {
            Set<QName> intents = helper.parseListOfQNames(reader, "intents");
            Set<QName> policySets = helper.parseListOfQNames(reader, "policySets");
            String attachTo = reader.getAttributeValue(null, "attachTo");
            if (attachTo == null) {
                context.addError(new MissingAttribute("Attribute attachTo must be specified", startLocation));
                attachTo = "";
            }
            ExternalAttachment attachment = new ExternalAttachment(attachTo, policySets, intents);
            validateAttributes(reader, context, attachment);
            LoaderUtil.skipToEndElement(reader);
            return attachment;
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
