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
package org.fabric3.introspection.xml.binding;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Type loader for the <f3:handler> element.
 */
@EagerInit
public class BindingHandlerLoader extends AbstractValidatingTypeLoader<BindingHandler> {
    private static final BindingHandler INVALID_DEFINITION = new BindingHandler(URI.create("Invalid"));

    public BindingHandlerLoader() {
        addAttributes("target");
    }

    public BindingHandler load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String target = reader.getAttributeValue(null, "target");
        if (target == null || "".equals(target)) {
            InvalidValue error = new InvalidValue("Target attribute is not specified", startLocation);
            context.addError(error);
            // catch invalid attributes as well
            validateAttributes(reader, context, INVALID_DEFINITION);
            return INVALID_DEFINITION;
        }
        try {
            URI targetUri = new URI(target);
            BindingHandler definition = new BindingHandler(targetUri);
            validateAttributes(reader, context, definition);
            return definition;
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Target attribute is not a valid URI: " + target, startLocation);
            context.addError(error);
            // catch invalid attributes as well
            validateAttributes(reader, context, INVALID_DEFINITION);
            return INVALID_DEFINITION;
        }
    }


}
