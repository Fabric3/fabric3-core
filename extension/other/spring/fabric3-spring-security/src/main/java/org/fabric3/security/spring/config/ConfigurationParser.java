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
package org.fabric3.security.spring.config;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.host.ContainerException;

/**
 * Parses a Spring security configuration.
 */
public interface ConfigurationParser {

    /**
     * Parses the configuration.
     *
     * @param reader the XML stream reader
     * @return the parsed configuration
     * @throws XMLStreamException             if an error occurs parsing the XML
     * @throws ContainerException if an invalid security configuration is specified
     */
    AuthenticationManagerConfiguration parse(XMLStreamReader reader) throws XMLStreamException, ContainerException;
}
