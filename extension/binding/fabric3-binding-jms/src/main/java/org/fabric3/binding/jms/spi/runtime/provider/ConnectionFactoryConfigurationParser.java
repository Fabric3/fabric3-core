package org.fabric3.binding.jms.spi.runtime.provider;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;

/**
 * Parses a connection factory configuration from an XML stream.
 */
public interface ConnectionFactoryConfigurationParser {

    /**
     * Parses the configuration.
     *
     * @param reader the XML stream
     * @return the configuration
     * @throws InvalidConfigurationException if the configuration contains an error
     * @throws XMLStreamException            if there is an error parsing the stream
     */
    ConnectionFactoryConfiguration parse(XMLStreamReader reader) throws InvalidConfigurationException, XMLStreamException;

}
