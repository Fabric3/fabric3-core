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
package org.fabric3.jndi.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fabric3.api.model.type.resource.jndi.JndiContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.EagerInit;

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
public class JndiContextLoader implements TypeLoader<JndiContext> {

    public JndiContext load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Map<String, Properties> contexts = new HashMap<>();
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
                    return new JndiContext(contexts);
                }
            }
        }
    }

}