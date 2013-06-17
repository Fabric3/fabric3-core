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
package org.fabric3.jndi.introspection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.jndi.model.JndiContextDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * Loads JNDI context configuration specified in a composite. The format of the <code>jndi</code> element is:
 * <pre>
 *      &lt;jndi&gt;
 *          &lt;context name="RemoteContext1"&gt;
 *              &lt;property name="name1" value="value1"/&gt;
 *          &lt;/context&gt;
 *          &lt;context name="RemoteContext2"&gt;
 *              &lt;property name="name1" value="value1"/&gt;
 *          &lt;/context&gt;
 *      &lt;/jndi&gt;
 * </pre>
 */
@EagerInit
public class JndiContextLoader implements TypeLoader<JndiContextDefinition> {

    public JndiContextDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Map<String, Properties> contexts = new HashMap<String, Properties>();
        Properties properties = null;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                Location location = reader.getLocation();
                if ("context".equals(reader.getName().getLocalPart())) {
                    String name = reader.getAttributeValue(null, "name");
                    if (name == null) {
                        MissingAttribute error = new MissingAttribute("Missing context name", location);
                        context.addError(error);
                        continue;
                    }
                    properties = new Properties();
                    contexts.put(name, properties);
                } else if ("property".equals(reader.getName().getLocalPart())) {
                    if (properties == null) {
                        InvalidValue error = new InvalidValue("Invalid JNDI configuration", location);
                        context.addError(error);
                        continue;
                    }
                    String name = reader.getAttributeValue(null, "name");
                    if (name == null) {
                        MissingAttribute error = new MissingAttribute("Missing property name", location);
                        context.addError(error);
                        continue;
                    }
                    String value = reader.getAttributeValue(null, "value");
                    if (value == null) {
                        MissingAttribute error = new MissingAttribute("Missing property value", location);
                        context.addError(error);
                        continue;
                    }
                    properties.put(name, value);
                }

                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("jndi".equals(reader.getName().getLocalPart())) {
                    return new JndiContextDefinition(contexts);
                }
            }
        }
    }

}