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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
