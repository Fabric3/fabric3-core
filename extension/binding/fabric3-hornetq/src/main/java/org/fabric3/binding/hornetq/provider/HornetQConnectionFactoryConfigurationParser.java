package org.fabric3.binding.hornetq.provider;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryConfigurationParser;
import org.fabric3.binding.jms.spi.runtime.provider.InvalidConfigurationException;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Parses {@link HornetQConnectionFactoryConfigurationParser} entries from a StAX source; entries may be connection factories or connection factory
 * templates.
 */
@EagerInit
public class HornetQConnectionFactoryConfigurationParser implements ConnectionFactoryConfigurationParser {

    public ConnectionFactoryConfiguration parse(XMLStreamReader reader) throws InvalidConfigurationException, XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            invalidConfiguration("Connection factory name not configured", reader, null);
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
                    invalidConfiguration("Unrecognized element " + localPart + " in system configuration", reader, null);
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

    private void invalidConfiguration(String message, XMLStreamReader reader, Exception e) throws InvalidConfigurationException {
        Location location = reader.getLocation();
        if (location == null) {
            // runtime has no external config file
            if (e != null) {
                throw new InvalidConfigurationException(message, e);
            }
            throw new InvalidConfigurationException(message);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        if (e != null) {
            throw new InvalidConfigurationException(message + " [" + line + "," + col + "]", e);
        }
        throw new InvalidConfigurationException(message + " [" + line + "," + col + "]");
    }


}
