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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Contains functionality for validating element attributes.
 */
public abstract class AbstractValidatingTypeLoader<OUTPUT> implements TypeLoader<OUTPUT> {
    protected Set<String> attributes = new HashSet<>();

    protected void addAttributes(String... attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException("Attributes cannot be null");
        }
        this.attributes.addAll(Arrays.asList(attributes));
    }

    protected void validateAttributes(XMLStreamReader reader, IntrospectionContext context, ModelObject... sources) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!attributes.contains(name)) {
                UnrecognizedAttribute failure = new UnrecognizedAttribute(name, location, sources);
                context.addError(failure);
            }
        }
    }


}
