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
package org.fabric3.binding.rs.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.binding.rs.model.RsBinding;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class RsBindingLoader extends AbstractValidatingTypeLoader<RsBinding> {

    public RsBindingLoader() {
        addAttributes("requires", "name", "policySets", "uri");
    }

    public RsBinding load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String bindingName = reader.getAttributeValue(null, "name");
        String uriStr = reader.getAttributeValue(null, "uri");
        URI uri;

        if (uriStr == null) {
            MissingAttribute failure = new MissingAttribute("URI not specified", startLocation);
            context.addError(failure);
            return null;
        }
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("Invalid URI value", startLocation);
            context.addError(failure);
            return null;
        }
        RsBinding binding = new RsBinding(bindingName, uri);

        validateAttributes(reader, context, binding);

        LoaderUtil.skipToEndElement(reader);
        return binding;
    }
}
