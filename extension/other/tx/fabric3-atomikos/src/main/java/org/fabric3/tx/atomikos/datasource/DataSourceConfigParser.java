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
package org.fabric3.tx.atomikos.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;

/**
 *
 */
public class DataSourceConfigParser {

    public List<DataSourceConfiguration> parse(XMLStreamReader reader) throws DataSourceParseException {
        List<DataSourceConfiguration> configurations = new ArrayList<DataSourceConfiguration>();
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
            throw new DataSourceParseException(e);
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

    private DataSourceType parseType(XMLStreamReader reader) throws DataSourceParseException {
        DataSourceType dataSourceType;
        String type = readMandatoryAttribute("type", reader);
        if (type == null) {
            dataSourceType = DataSourceType.NON_XA;
        } else {
            try {
                dataSourceType = DataSourceType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new DataSourceParseException("Datasource type must be either xa or non_xa");
            }
        }
        return dataSourceType;
    }

    private int parseInt(String value, String name) throws DataSourceParseException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new DataSourceParseException("Invalid value for " + name, e);
        }
    }

    private String readMandatoryAttribute(String name, XMLStreamReader reader) throws DataSourceParseException {
        String val = reader.getAttributeValue(null, name);
        if (val == null) {
            Location location = reader.getLocation();
            if (location == null) {
                // configuration does not come from the file system
                throw new DataSourceParseException("Datasource " + name + " not specified in system configuration");
            }
            int line = location.getLineNumber();
            int col = location.getColumnNumber();
            throw new DataSourceParseException("Datasource " + name + " not configured [" + line + "," + col + "]");
        }
        return val;
    }
}