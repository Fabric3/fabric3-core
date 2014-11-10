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
 */
package org.fabric3.binding.web.loader;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.model.WebBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;

/**
 * Loads <code>binding.web</code> elements in a composite.
 */
@EagerInit
public class WebBindingLoader extends AbstractValidatingTypeLoader<WebBindingDefinition> {
    private LoaderHelper loaderHelper;

    public WebBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        addAttributes("allowed", "name", "policySets", "requires");
    }

    public WebBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String bindingName = reader.getAttributeValue(null, "name");
        OperationsAllowed allowed = OperationsAllowed.ALL;
        String allowedStr = reader.getAttributeValue(null, "allowed");
        if (allowedStr != null) {
            try {
                allowed = OperationsAllowed.valueOf(allowedStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidValue("Invalid allowed type: " + allowedStr, startLocation));
            }
        }

        String wireFormat = reader.getAttributeValue(null, "wireFormat");
        if (wireFormat != null) {
            if (!wireFormat.equalsIgnoreCase("json") || !wireFormat.equalsIgnoreCase("xml")) {
                InvalidValue error = new InvalidValue("Invalid wire format: " + wireFormat, startLocation);
                context.addError(error);
            }
        }
        WebBindingDefinition definition = new WebBindingDefinition(bindingName, allowed, wireFormat);
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        validateAttributes(reader, context, definition);

        LoaderUtil.skipToEndElement(reader);
        return definition;
    }


}
