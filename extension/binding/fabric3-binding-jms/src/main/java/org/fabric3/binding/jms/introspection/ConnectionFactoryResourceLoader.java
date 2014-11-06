/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.binding.jms.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;

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