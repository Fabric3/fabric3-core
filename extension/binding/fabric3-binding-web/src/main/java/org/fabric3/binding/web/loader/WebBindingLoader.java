/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.web.loader;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.model.WebBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;

/**
 * Loads <code>binding.web</code> elements in a composite.
 */
@EagerInit
public class WebBindingLoader extends AbstractValidatingTypeLoader<WebBindingDefinition> {
    private LoaderHelper loaderHelper;

    public WebBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        addAttributes("allowed", "name", "policySets", "requires");
    }

    public WebBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String bindingName = reader.getAttributeValue(null, "name");
        OperationsAllowed allowed = OperationsAllowed.ALL;
        String allowedStr = reader.getAttributeValue(null, "allowed");
        if (allowedStr != null) {
            try {
                allowed = OperationsAllowed.valueOf(allowedStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                context.addError(new InvalidValue("Invalid allowed type: " + allowedStr, startLocation));
            }
        }

        String wireFormat = reader.getAttributeValue(null, "wireFormat");
        if (wireFormat != null) {
            if (!wireFormat.equalsIgnoreCase("json") || !wireFormat.equalsIgnoreCase("xml")) {
                InvalidValue error = new InvalidValue("Invalid wire format: " + wireFormat, startLocation);
                context.addError(error);
            }
        }
        WebBindingDefinition definition = new WebBindingDefinition(bindingName, allowed, wireFormat);
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        validateAttributes(reader, context, definition);

        LoaderUtil.skipToEndElement(reader);
        return definition;
    }


}
