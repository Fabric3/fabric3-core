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
package org.fabric3.datasource.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceResource;
import org.fabric3.api.model.type.resource.datasource.DataSourceType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Loads datasource configurations specified in a composite. The format of the datasources element is:
 * <pre>
 *      &lt;datasources&gt;
 *          &lt;datasource name="test" driver="foo.Bar" type="xa" url="jdbc:test"&gt;
 *               &lt;username>user&lt;username&gt;
 *               &lt;password>pass&lt;password&gt;
 *           &lt;/datasource&gt;
 *      &lt;/datasources&gt;
 * </pre>
 */
@EagerInit
public class DataSourceResourceLoader implements TypeLoader<DataSourceResource> {

    public DataSourceResource load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        List<DataSourceConfiguration> configurations = new ArrayList<>();
        DataSourceConfiguration configuration = null;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                Location location = reader.getLocation();
                if ("datasource".equals(reader.getName().getLocalPart())) {
                    String name = readMandatoryAttribute("name", reader, context);
                    DataSourceType dataSourceType;
                    String type = reader.getAttributeValue(null, "type");
                    if (type == null) {
                        dataSourceType = DataSourceType.NON_XA;
                    } else {
                        try {
                            dataSourceType = DataSourceType.valueOf(type.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            InvalidValue error = new InvalidValue("Datasource type must be either xa or non_xa", location, e);
                            context.addError(error);
                            dataSourceType = DataSourceType.NON_XA;
                        }
                    }
                    String driver = readMandatoryAttribute("driver", reader, context);
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
                            configuration.setMaxPoolSize(parseInt(value, location, context));
                        } else if ("minPoolSize".equals(name)) {
                            configuration.setMinPoolSize(parseInt(value, location, context));
                        } else if ("connectionTimeout".equals(name)) {
                            configuration.setConnectionTimeout(parseInt(value, location, context));
                        } else if ("loginTimeout".equals(name)) {
                            configuration.setLoginTimeout(parseInt(value, location, context));
                        } else if ("maintenanceInterval".equals(name)) {
                            configuration.setMaintenanceInterval(parseInt(value, location, context));
                        } else if ("maxIdle".equals(name)) {
                            configuration.setMaxIdle(parseInt(value, location, context));
                        } else if ("poolSize".equals(name)) {
                            configuration.setPoolSize(parseInt(value, location, context));
                        } else if ("reap".equals(name)) {
                            configuration.setReap(parseInt(value, location, context));
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
                if ("datasources".equals(reader.getName().getLocalPart())) {
                    return new DataSourceResource(configurations);
                }
            }
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

    private String readMandatoryAttribute(String name, XMLStreamReader reader, IntrospectionContext context) {
        String val = reader.getAttributeValue(null, name);
        if (val == null) {
            InvalidValue error = new InvalidValue("Datasource " + name + " must be specified", reader.getLocation());
            context.addError(error);
        }
        return val;
    }

    private int parseInt(String value, Location location, IntrospectionContext context) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            context.addError(new InvalidValue("Invalid value", location, e));
            return 0;
        }
    }
}