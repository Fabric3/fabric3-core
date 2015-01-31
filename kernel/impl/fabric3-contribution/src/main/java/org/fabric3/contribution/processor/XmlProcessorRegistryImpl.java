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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default implementation of an XmlProcessorRegistry.
 */
public class XmlProcessorRegistryImpl implements XmlProcessorRegistry {
    private Map<QName, XmlProcessor> cache = new HashMap<>();

    public void register(XmlProcessor processor) {
        cache.put(processor.getType(), processor);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    public void process(Contribution contribution, XMLStreamReader reader, IntrospectionContext context) throws ContainerException {
        QName name = reader.getName();
        XmlProcessor processor = cache.get(name);
        if (processor == null) {
            String id = name.toString();
            throw new ContainerException("XML processor not found for: " + id);
        }
        processor.processContent(contribution, reader, context);
    }
}