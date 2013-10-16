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

import org.fabric3.api.host.Namespaces;
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
    private static final QName QNAME = new QName(Namespaces.F3, "template");
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
