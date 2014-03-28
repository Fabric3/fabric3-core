package org.fabric3.binding.hornetq.provider;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.introspection.ConnectionFactoryConfigurationParser;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Parses {@link HornetQConnectionFactoryConfigurationParser} entries from a StAX source; entries may be connection factories or connection factory templates.
 */
@EagerInit
public class HornetQConnectionFactoryConfigurationParser implements ConnectionFactoryConfigurationParser {

    public ConnectionFactoryConfiguration parse(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        Location location = reader.getLocation();
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Connection factory name not configured", location, null);
            context.addError(error);
        }
        ConnectionFactoryConfiguration configuration = new ConnectionFactoryConfiguration(name, "hornetmq");

        String typeString = reader.getAttributeValue(null, "type");
        if (typeString != null) {
            ConnectionFactoryType type = ConnectionFactoryType.valueOf(typeString.trim().toUpperCase());
            configuration.setType(type);
        }

        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    String localPart = reader.getName().getLocalPart();
                    if ("parameters".equals(localPart)) {
                        parseParameters(configuration, reader);
                    } else {
                        UnrecognizedAttribute error = new UnrecognizedAttribute("Unrecognized element " + localPart + " in system configuration",
                                                                                location,
                                                                                null);
                        context.addError(error);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (reader.getName().getLocalPart().startsWith("connection.factory")) {
                        return configuration;
                    }
            }
        }
    }

    private void parseParameters(ConnectionFactoryConfiguration configuration, XMLStreamReader reader) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    configuration.addAttribute(reader.getName().getLocalPart(), reader.getElementText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("factory.properties".equals(reader.getName().getLocalPart())) {
                        return;
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return;
            }
        }
    }

}
