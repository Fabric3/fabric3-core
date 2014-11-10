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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
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

/**
 * Parses broker configuration from the runtime system configuration.
 */
public class BrokerParser {


    public BrokerConfiguration parse(XMLStreamReader reader) throws XMLStreamException, InvalidBrokerConfigurationException {
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
                return configuration;
            }
        }
    }

    private void parsePersistenceAdapter(XMLStreamReader reader, BrokerConfiguration configuration) throws InvalidBrokerConfigurationException {
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
            throws XMLStreamException, InvalidBrokerConfigurationException {
        List<TransportConnectorConfig> transportConfigs = new ArrayList<>();
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
                            raiseInvalidConfiguration("Invalid discovery uri", e, reader);
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
            throws XMLStreamException, InvalidBrokerConfigurationException {
        List<URI> uris = new ArrayList<>();
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

    private void raiseInvalidConfiguration(String message, XMLStreamReader reader) throws InvalidBrokerConfigurationException {
        Location location = reader.getLocation();
        if (location == null) {
            throw new InvalidBrokerConfigurationException(message);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        throw new InvalidBrokerConfigurationException(message + " [" + line + "," + col + "]");
    }

    private void raiseInvalidConfiguration(String message, Throwable e, XMLStreamReader reader) throws InvalidBrokerConfigurationException {
        Location location = reader.getLocation();
        if (location == null) {
            throw new InvalidBrokerConfigurationException(message, e);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        throw new InvalidBrokerConfigurationException(message + " [" + line + "," + col + "]", e);
    }

}


