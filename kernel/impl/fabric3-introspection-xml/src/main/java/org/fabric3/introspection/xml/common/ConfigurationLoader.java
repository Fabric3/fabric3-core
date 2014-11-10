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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.common;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Loads a configuration element of the form:
 * <pre>
 *
 *     &lt;configuration&gt;
 *        &lt;key1&gt;value1&lt;/key1&gt;
 *        &lt;key2&gt;value2&lt;/key2&gt;
 *     &lt;/configuration&gt;
 * </pre>
 * as a Map of String keys and values.
 */
@EagerInit
public class ConfigurationLoader implements TypeLoader<Map<String, String>> {


    public Map<String, String> load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Map<String, String> configuration = new LinkedHashMap<>();
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                String name = reader.getName().getLocalPart();
                String value = reader.getElementText();
                configuration.put(name, value);
                break;
            case END_ELEMENT:
                name = reader.getName().getLocalPart();
                if ("configuration".equals(name)) {
                    return configuration;
                }
                break;
            }

        }

    }


}
