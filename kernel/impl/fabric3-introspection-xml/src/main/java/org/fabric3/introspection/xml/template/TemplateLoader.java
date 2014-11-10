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
package org.fabric3.introspection.xml.template;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 * General class for loading template definitions such as <code>&lt;binding.template&gt;</code>. This class is general and is designed to be
 * configured to support specific element types. It works by resolving the requested template using the {@link TemplateRegistry}.
 */
@EagerInit
public class TemplateLoader extends AbstractValidatingTypeLoader<ModelObject> {
    private TemplateRegistry registry;
    private Class<? extends ModelObject> expectedType;

    @SuppressWarnings({"unchecked"})
    public TemplateLoader(@Reference TemplateRegistry registry, @Property(name = "expectedType") String expectedType) {
        this.registry = registry;
        try {
            this.expectedType = (Class<? extends ModelObject>) getClass().getClassLoader().loadClass(expectedType);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        addAttributes("name");
    }

    public ModelObject load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Attribute name must be specified", startLocation);
            context.addError(error);
            LoaderUtil.skipToEndElement(reader);
            return null;
        }
        ModelObject parsed = registry.resolve(expectedType, name);
        if (parsed == null) {
            TemplateNotFound error = new TemplateNotFound(name, startLocation);
            context.addError(error);
        }
        LoaderUtil.skipToEndElement(reader);
        return parsed;
    }

}
