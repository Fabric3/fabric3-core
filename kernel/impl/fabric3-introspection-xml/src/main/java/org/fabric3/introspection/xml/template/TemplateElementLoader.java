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

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.DuplicateTemplateException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 * Loads a <code>&lt;template&gt;</code> element in a composite.
 */
@EagerInit
public class TemplateElementLoader extends AbstractValidatingTypeLoader<ModelObject> {
    private static final QName QNAME = new QName(org.fabric3.api.Namespaces.F3, "template");
    private static final QName LAX_QNAME = new QName("", "template");

    private LoaderRegistry loaderRegistry;
    private TemplateRegistry templateRegistry;

    public TemplateElementLoader(@Reference LoaderRegistry loaderRegistry, @Reference TemplateRegistry templateRegistry) {
        this.loaderRegistry = loaderRegistry;
        this.templateRegistry = templateRegistry;
        addAttributes("name");
    }

    @Init
    public void init() {
        loaderRegistry.registerLoader(QNAME, this);
        loaderRegistry.registerLoader(LAX_QNAME, this);
    }

    @Destroy
    public void destroy() {
        loaderRegistry.unregisterLoader(QNAME);
        loaderRegistry.unregisterLoader(LAX_QNAME);
    }

    public ModelObject load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Template name not specified", startLocation);
            context.addError(error);
            LoaderUtil.skipToEndElement(reader);
            return null;
        }
        int val = reader.nextTag();
        if (val == XMLStreamConstants.END_ELEMENT && reader.getName().getLocalPart().equals("template")) {
            InvalidTemplateDefinition error = new InvalidTemplateDefinition("Template body is missing: " + name, startLocation);
            context.addError(error);
            return null;
        }
        try {
            URI uri = context.getContributionUri();
            ModelObject parsed = loaderRegistry.load(reader, ModelObject.class, context);
            templateRegistry.register(name, uri, parsed);
            LoaderUtil.skipToEndElement(reader);
        } catch (DuplicateTemplateException e) {
            DuplicateTemplate error = new DuplicateTemplate(name, startLocation);
            context.addError(error);
        }
        return null;
    }


}
