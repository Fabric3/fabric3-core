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
package org.fabric3.binding.test;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Parses <code>binding.test</code> for services and references. A uri to bind the service to or target a reference must be provided as an attribute.
 */
@EagerInit
@Key(Namespaces.F3_PREFIX + "binding.test")
public class TestBindingLoader implements TypeLoader<TestBinding> {

    public TestBinding load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        TestBinding binding = null;
        String uri = null;
        try {
            URI targetUri = null;
            uri = reader.getAttributeValue(null, "uri");
            if (uri != null) {
                targetUri = new URI(uri);
            }
            String name = reader.getAttributeValue(null, "name");
            binding = new TestBinding(name, targetUri);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The binding URI is not valid: " + uri, startLocation);
            context.addError(failure);
        }
        LoaderUtil.skipToEndElement(reader);
        return binding;

    }

}
