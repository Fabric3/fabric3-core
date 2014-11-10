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
package org.fabric3.binding.hornetq.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * Loads a HornetQ connection factory configuration specified in a composite. The format of the <code>connection.factory.hornetq</code> element is:
 * <pre>
 *      &lt;connection.factory.hornetq&gt;
 *      &lt;/connection.factory.hornetq&gt;
 * </pre>
 */
@EagerInit
public class HornetQConnectionFactoryLoader implements TypeLoader<HornetQConnectionFactoryDefinition> {

    public HornetQConnectionFactoryDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

}