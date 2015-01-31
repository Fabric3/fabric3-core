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
package org.fabric3.tx.atomikos.datasource;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;

/**
 *
 */
public class DataSourceConfigParser {

    public List<DataSourceConfiguration> parse(XMLStreamReader reader) throws ContainerException {
        List<DataSourceConfiguration> configurations = new ArrayList<>();
        try {
            reader.nextTag();
            DataSourceConfiguration configuration = null;
            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if ("datasource".equals(reader.getName().getLocalPart())) {
                        String name = readMandatoryAttribute("name", reader);

                        DataSourceType dataSourceType = parseType(reader);

                        String driver = readMandatoryAttribute("driver", reader);
                        configuration = new DataSourceConfiguration(name, driver, dataSourceType);

                        List<String> aliases = readAliases(reader);
                        configuration.setAliases(aliases);

                        String url = reader.getAttributeValue(null, "url");
                        configuration.setUrl(url);
                        String username = reader.getAttributeValue(null, "username");
                        configuration.setUsername(username);
                        String password = reader.getAttributeValue(null, "password");
                        configuration.setPassword(password);
                    } else {
                        // check to ensure the <datasource> element comes before a property or other element
                        if (configuration != null) {
                            // read property
                            String name = reader.getName().getLocalPart();
                            String value = reader.getElementText();

                            if ("maxPoolSize".equals(name)) {
                                configuration.setMaxPoolSize(parseInt(value, "maxPoolSize"));
                            } else if ("minPoolSize".equals(name)) {
                                configuration.setMinPoolSize(parseInt(value, "minPoolSize"));
                            } else if ("connectionTimeout".equals(name)) {
                                configuration.setConnectionTimeout(parseInt(value, "connectionTimeout"));
                            } else if ("loginTimeout".equals(name)) {
                                configuration.setLoginTimeout(parseInt(value, "loginTimeout"));
                            } else if ("maintenanceInterval".equals(name)) {
                                configuration.setMaintenanceInterval(parseInt(value, "maintenanceInterval"));
                            } else if ("maxIdle".equals(name)) {
                                configuration.setMaxIdle(parseInt(value, "maxIdle"));
                            } else if ("poolSize".equals(name)) {
                                configuration.setPoolSize(parseInt(value, "poolSize"));
                            } else if ("reap".equals(name)) {
                                configuration.setReap(parseInt(value, "reap"));
                            } else if ("query".equals(name)) {
                                configuration.setQuery(value);
                            } else {
                                configuration.setProperty(name, value);
                            }

                        }
                    }

                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("datasource".equals(reader.getName().getLocalPart())) {
                        configurations.add(configuration);
                        break;
                    }
                case XMLStreamConstants.END_DOCUMENT:
                    return configurations;
                }
            }
        } catch (XMLStreamException e) {
            throw new ContainerException(e);
        }
    }

    private List<String> readAliases(XMLStreamReader reader) {
        String aliasesAttr = reader.getAttributeValue(null, "aliases");
        if (aliasesAttr == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(aliasesAttr.split(","));
        }
    }

    private DataSourceType parseType(XMLStreamReader reader) throws ContainerException {
        DataSourceType dataSourceType;
        String type = readMandatoryAttribute("type", reader);
        if (type == null) {
            dataSourceType = DataSourceType.NON_XA;
        } else {
            try {
                dataSourceType = DataSourceType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ContainerException("Datasource type must be either xa or non_xa");
            }
        }
        return dataSourceType;
    }

    private int parseInt(String value, String name) throws ContainerException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ContainerException("Invalid value for " + name, e);
        }
    }

    private String readMandatoryAttribute(String name, XMLStreamReader reader) throws ContainerException {
        String val = reader.getAttributeValue(null, name);
        if (val == null) {
            Location location = reader.getLocation();
            if (location == null) {
                // configuration does not come from the file system
                throw new ContainerException("Datasource " + name + " not specified in system configuration");
            }
            int line = location.getLineNumber();
            int col = location.getColumnNumber();
            throw new ContainerException("Datasource " + name + " not configured [" + line + "," + col + "]");
        }
        return val;
    }
}