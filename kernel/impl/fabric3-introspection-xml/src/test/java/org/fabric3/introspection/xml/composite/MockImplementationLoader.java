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
package org.fabric3.introspection.xml.composite;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 *
 */
public class MockImplementationLoader implements TypeLoader<MockImplementation> {
    private Property[] properties;
    private Reference<ComponentType>[] references;
    private Service<ComponentType>[] services;

    public MockImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        MockImplementation impl = new MockImplementation();
        InjectingComponentType type = new InjectingComponentType();
        impl.setComponentType(type);
        if (properties != null) {
            for (Property property : properties) {
                type.add(property);
            }
        }
        if (references != null) {
            for (Reference<ComponentType> definition : references) {
                type.add(definition);
            }
        }
        if (services != null) {
            for (Service<ComponentType> definition : services) {
                type.add(definition);
            }
        }
        reader.next();
        return impl;
    }

    public void setProperties(Property... properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    public void setReferences(Reference... references) {
        this.references = references;
    }

    @SuppressWarnings("unchecked")
    public void setServices(Service... services) {
        this.services = services;
    }

}
