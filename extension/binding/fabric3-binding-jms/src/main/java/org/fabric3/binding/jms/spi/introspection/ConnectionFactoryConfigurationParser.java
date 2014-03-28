package org.fabric3.binding.jms.spi.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Parses a connection factory configuration from an XML stream.
 */
public interface ConnectionFactoryConfigurationParser {

    /**
     * Parses the configuration.
     *
     * @param reader  the XML stream
     * @param context the introspection context
     * @return the configuration
     * @throws XMLStreamException if there is an error parsing the stream
     */
    ConnectionFactoryConfiguration parse(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException;

}
