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
package org.fabric3.binding.activemq.broker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.activemq.factory.InvalidConfigurationException;

/**
 * Parses broker configuration from the runtime system configuration.
 *
 * @version $Rev$ $Date$
 */
public class BrokerParser {


    public BrokerConfiguration parse(XMLStreamReader reader) throws XMLStreamException, InvalidConfigurationException {
        reader.nextTag();
        BrokerConfiguration configuration = new BrokerConfiguration();
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("broker".equals(reader.getName().getLocalPart())) {
                    String name = reader.getAttributeValue(null, "name");
                    configuration.setName(name);
                } else if ("networkConnectors".equals(reader.getName().getLocalPart())) {
                    parseNetworkConnectors(reader, configuration);
                } else if ("transportConnectors".equals(reader.getName().getLocalPart())) {
                    parseTransportConnectors(reader, configuration);
                } else if ("persistenceAdapter".equals(reader.getName().getLocalPart())) {
                    parsePersistenceAdapter(reader, configuration);
                }

                break;
            case XMLStreamConstants.END_DOCUMENT:
                if (configuration.getName() == null) {
                    raiseInvalidConfiguration("Broker name must be specified", reader);
                }
                return configuration;
            }
        }
    }

    private void parsePersistenceAdapter(XMLStreamReader reader, BrokerConfiguration configuration)
            throws XMLStreamException, InvalidConfigurationException {
        String type = reader.getAttributeValue(null, "type");
        PersistenceAdapterConfig adaptorConfig = new PersistenceAdapterConfig();
        if (type == null) {
            type = "amq"; // default to AMQ
        }
        if ("amq".equalsIgnoreCase(type)) {
            boolean syncOnWrite = Boolean.valueOf(reader.getAttributeValue(null, "syncOnWrite"));
            adaptorConfig.setSyncOnWrite(syncOnWrite);
            String maxFileLength = reader.getAttributeValue(null, "maxFileLength");
            if (maxFileLength != null) {
                adaptorConfig.setMaxFileLength(maxFileLength);
            }
            String checkpointInterval = reader.getAttributeValue(null, "checkpointInterval");
            if (checkpointInterval != null) {
                try {
                    adaptorConfig.setCheckpointInterval(Long.valueOf(checkpointInterval));
                } catch (NumberFormatException e) {
                    raiseInvalidConfiguration("Invalid check point interval", e, reader);
                }
            }

            String cleanupInterval = reader.getAttributeValue(null, "cleanupInterval");
            if (cleanupInterval != null) {
                try {
                    adaptorConfig.setCleanupInterval(Long.valueOf(cleanupInterval));
                } catch (NumberFormatException e) {
                    raiseInvalidConfiguration("Invalid cleanup interval", e, reader);
                }
            }
            boolean disableLocking = Boolean.valueOf(reader.getAttributeValue(null, "disableLocking"));
            adaptorConfig.setDisableLocking(disableLocking);

            String indexBinSize = reader.getAttributeValue(null, "indexBinSize");
            if (indexBinSize != null) {
                try {
                    adaptorConfig.setIndexBinSize(Integer.valueOf(indexBinSize));
                } catch (NumberFormatException e) {
                    raiseInvalidConfiguration("Invalid index bin size", e, reader);
                }
            }

            String indexKeySize = reader.getAttributeValue(null, "indexKeySize");
            if (indexKeySize != null) {
                try {
                    adaptorConfig.setIndexKeySize(Integer.valueOf(indexKeySize));
                } catch (NumberFormatException e) {
                    raiseInvalidConfiguration("Invalid index key size", e, reader);
                }
            }

            String indexPageSize = reader.getAttributeValue(null, "indexPageSize");
            if (indexBinSize != null) {
                try {
                    adaptorConfig.setIndexPageSize(Integer.valueOf(indexPageSize));
                } catch (NumberFormatException e) {
                    raiseInvalidConfiguration("Invalid index page size", e, reader);
                }
            }

        } else {
            raiseInvalidConfiguration("Persistence adaptor type configuration not supported:" + type, reader);
        }
        configuration.setPersistenceAdapter(adaptorConfig);
    }

    private void parseTransportConnectors(XMLStreamReader reader, BrokerConfiguration configuration)
            throws XMLStreamException, InvalidConfigurationException {
        List<TransportConnectorConfig> transportConfigs = new ArrayList<TransportConnectorConfig>();
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("transportConnector".equals(reader.getName().getLocalPart())) {
                    URI uri = null;
                    String uriString = reader.getAttributeValue(null, "uri");
                    if (uriString == null) {
                        raiseInvalidConfiguration("Network transport connector uri not specified", reader);
                    }
                    try {
                        uri = new URI(uriString);
                    } catch (URISyntaxException e) {
                        raiseInvalidConfiguration("Invalid transport connector uri", e, reader);
                    }
                    TransportConnectorConfig transportConfig = new TransportConnectorConfig();
                    transportConfig.setUri(uri);
                    String discoveryUriString = reader.getAttributeValue(null, "discoveryUri");
                    if (discoveryUriString != null) {
                        try {
                            transportConfig.setDiscoveryUri(new URI(discoveryUriString));
                        } catch (URISyntaxException e) {
                            raiseInvalidConfiguration("Invalid disovery uri", e, reader);
                        }
                    }
                    transportConfigs.add(transportConfig);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("transportConnectors".equals(reader.getName().getLocalPart())) {
                    configuration.setTransportConnectorConfigs(transportConfigs);
                    return;
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                throw new AssertionError("End of document encountered");

            }
        }

    }

    private void parseNetworkConnectors(XMLStreamReader reader, BrokerConfiguration configuration)
            throws XMLStreamException, InvalidConfigurationException {
        List<URI> uris = new ArrayList<URI>();
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("networkConnector".equals(reader.getName().getLocalPart())) {
                    String uriString = reader.getAttributeValue(null, "uri");
                    if (uriString == null) {
                        raiseInvalidConfiguration("Network connector uri not specified", reader);
                    }
                    try {
                        URI uri = new URI(uriString);
                        uris.add(uri);
                    } catch (URISyntaxException e) {
                        raiseInvalidConfiguration("Invalid network connector uri", e, reader);
                    }
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("networkConnectors".equals(reader.getName().getLocalPart())) {
                    configuration.setNetworkConnectorUris(uris);
                    return;
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                throw new AssertionError("End of document encountered");

            }
        }
    }

    private void raiseInvalidConfiguration(String message, XMLStreamReader reader) throws InvalidConfigurationException {
        Location location = reader.getLocation();
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        throw new InvalidConfigurationException(message + " [" + line + "," + col + "]");
    }

    private void raiseInvalidConfiguration(String message, Throwable e, XMLStreamReader reader) throws InvalidConfigurationException {
        Location location = reader.getLocation();
        if (location == null) {
            throw new InvalidConfigurationException(message, e);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        throw new InvalidConfigurationException(message + " [" + line + "," + col + "]", e);
    }

}


