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
 */
package org.fabric3.binding.rs.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.binding.rs.model.RsBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 *
 */
@EagerInit
public class RsBindingLoader extends AbstractValidatingTypeLoader<RsBindingDefinition> {

    private final LoaderHelper loaderHelper;

    public RsBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        addAttributes("requires", "name", "policySets", "uri");
    }

    public RsBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String bindingName = reader.getAttributeValue(null, "name");
        String uriStr = reader.getAttributeValue(null, "uri");
        URI uri;

        if (uriStr == null) {
            MissingAttribute failure = new MissingAttribute("URI not specified", startLocation);
            context.addError(failure);
            return null;
        }
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("Invalid URI value", startLocation);
            context.addError(failure);
            return null;
        }
        RsBindingDefinition definition = new RsBindingDefinition(bindingName, uri);
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        validateAttributes(reader, context, definition);

        LoaderUtil.skipToEndElement(reader);
        return definition;
    }
}
