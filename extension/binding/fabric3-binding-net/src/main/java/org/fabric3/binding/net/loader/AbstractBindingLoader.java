/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.net.loader;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.net.config.BaseConfig;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Base binding loader.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public abstract class AbstractBindingLoader<T extends BindingDefinition> implements TypeLoader<T> {

    protected void parseResponse(XMLStreamReader reader, BaseConfig config, IntrospectionContext context) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if (reader.getName().getLocalPart().startsWith("wireFormat.")) {
                    parseWireFormat(reader, config, true, context);
                    break;
                }
            case XMLStreamConstants.END_ELEMENT:
                if ("response".equals(reader.getName().getLocalPart())) {
                    return;
                }
            }
        }
    }

    protected URI parseUri(XMLStreamReader reader, IntrospectionContext context) {
        String uriString = reader.getAttributeValue(null, "uri");
        if (uriString == null) {
            MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", reader);
            context.addError(failure);
            return null;
        }

        try {
            return new URI(uriString);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The binding URI is not valid: " + uriString, reader);
            context.addError(failure);
            return null;
        }
    }

    protected void parseBindingAttributes(XMLStreamReader reader, BaseConfig config, IntrospectionContext context) {
        String readTimeout = reader.getAttributeValue(null, "readTimeout");
        if (readTimeout != null) {
            try {
                long timeout = Long.parseLong(readTimeout);
                config.setReadTimeout(timeout);
            } catch (NumberFormatException e) {
                context.addError(new InvalidValue("Invalid timeout: " + readTimeout, reader, e));
            }
        }
        String numberOfRetries = reader.getAttributeValue(null, "numberOfRetries");
        if (numberOfRetries != null) {
            try {
                config.setNumberOfRetries(Integer.parseInt(numberOfRetries));
            } catch (NumberFormatException e) {
                InvalidValue failure = new InvalidValue("Invalid number of retries value ", reader);
                context.addError(failure);
            }
        }
        // validate
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"uri".equals(name) && !"readTimeout".equals(name) && !"numberOfRetries".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }

    }

    protected void parseWireFormat(XMLStreamReader reader, BaseConfig config, boolean response, IntrospectionContext context) {
        String name = reader.getName().getLocalPart();
        if (name.length() < 11) {
            InvalidWireFormat failure = new InvalidWireFormat("Invalid wire format: " + name, reader);
            context.addError(failure);
            return;
        }
        String format = name.substring(11); //wireFormat.

        if (response) {
            config.setResponseWireFormat(format);
        } else {
            config.setWireFormat(format);
        }

    }

    protected void parseSslSettings(XMLStreamReader reader, BaseConfig config, IntrospectionContext context) {
        String alias = reader.getAttributeValue(null, "alias");
        if (alias == null) {
            MissingAttribute failure = new MissingAttribute("An SSL alias must be specified ", reader);
            context.addError(failure);
            return;
        }
        config.setSslSettings(alias);

    }

}