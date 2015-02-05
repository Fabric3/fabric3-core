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
package org.fabric3.binding.jms.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryResource;
import org.fabric3.binding.jms.spi.introspection.ConnectionFactoryConfigurationParser;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads connection factory configurations specified in a composite. The format of the datasources element is:
 * <pre>
 *      &lt;connection.factory username='' password='' provider=''/&gt;
 * </pre>
 */
@EagerInit
@Key(Namespaces.F3_PREFIX + "connection.factory")
public class ConnectionFactoryResourceLoader implements TypeLoader<ConnectionFactoryResource> {

    @Reference(required = false)
    protected Map<String, ConnectionFactoryConfigurationParser> parsers = Collections.emptyMap();

    public ConnectionFactoryResource load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        if (parsers.isEmpty()) {
            // skip the resource
            return null;
        }
        String provider = reader.getAttributeValue(null, "provider");
        ConnectionFactoryConfigurationParser parser;
        if (provider == null) {
            parser = parsers.values().iterator().next();
        } else {
            parser = parsers.get(provider);
            if (parser == null) {
                UnrecognizedAttribute error = new UnrecognizedAttribute("JMS provider not installed: " + provider, reader.getLocation());
                context.addError(error);
                return null;
            }
        }

        ConnectionFactoryConfiguration configuration = parser.parse(reader, context);
        return new ConnectionFactoryResource(configuration);
    }
}