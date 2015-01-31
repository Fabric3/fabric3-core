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
package org.fabric3.monitor.spi.appender;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

import org.fabric3.api.host.ContainerException;

/**
 * Instantiates appenders from an XML configuration source.
 */
public interface AppenderFactory {

    /**
     * Instantiates a collection of default appenders.
     *
     * @return the appenders
     * @throws ContainerException if there is an error instantiating the appenders
     */
    List<Appender> instantiateDefaultAppenders() throws ContainerException;

    /**
     * Instantiates a collection of appenders from a configuration.
     *
     * @param reader the configuration source
     * @return the appenders
     * @throws ContainerException if there is an error instantiating the appenders
     * @throws XMLStreamException if there is an error parsing the configuration
     */
    List<Appender> instantiate(XMLStreamReader reader) throws ContainerException, XMLStreamException;

}
