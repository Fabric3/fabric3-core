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
package org.fabric3.binding.file.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.file.api.annotation.Strategy;
import org.fabric3.binding.file.api.model.FileBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * Loads a <code>binding.file</code> element in a composite.
 */
@EagerInit
public class FileBindingLoader extends AbstractValidatingTypeLoader<FileBindingDefinition> {
    private final LoaderHelper loaderHelper;

    public FileBindingLoader(@Reference LoaderHelper loaderHelper) {
        addAttributes("requires",
                      "location",
                      "archive.location",
                      "error.location",
                      "strategy",
                      "pattern",
                      "name",
                      "adapter",
                      "adapter.component",
                      "policySets",
                      "delay");
        this.loaderHelper = loaderHelper;
    }

    public FileBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String bindingName = reader.getAttributeValue(null, "name");

        String location = reader.getAttributeValue(null, "location");
        if (location == null) {
            MissingAttribute error = new MissingAttribute("The location attribute must be specified", startLocation);
            context.addError(error);
        }

        String archiveLocation = reader.getAttributeValue(null, "archive.location");

        Strategy strategy = parseStrategy(reader);
        if (Strategy.ARCHIVE == strategy && archiveLocation == null) {
            MissingAttribute error = new MissingAttribute("An archive location must be specified", startLocation);
            context.addError(error);
        }
        String errorLocation = reader.getAttributeValue(null, "error.location");

        String adapterClass = reader.getAttributeValue(null, "adapter");

        String adapterUri = reader.getAttributeValue(null, "adapter.component");

        String pattern = reader.getAttributeValue(null, "pattern");

        long delay = parseDelay(reader, context);
        FileBindingDefinition definition =
                new FileBindingDefinition(bindingName, pattern, location, strategy, archiveLocation, errorLocation, adapterClass, adapterUri, delay);

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        validateAttributes(reader, context, definition);

        LoaderUtil.skipToEndElement(reader);
        return definition;
    }

    private Strategy parseStrategy(XMLStreamReader reader) {
        String strategyAttr = reader.getAttributeValue(null, "strategy");
        if (strategyAttr == null || Strategy.DELETE.toString().toUpperCase().equals(strategyAttr)) {
            return Strategy.DELETE;
        } else {
            return Strategy.valueOf(strategyAttr.toUpperCase());
        }
    }

    private long parseDelay(XMLStreamReader reader, IntrospectionContext context) {
        long delay = -1;
        String delayStr = reader.getAttributeValue(null, "delay");
        if (delayStr != null) {
            try {
                delay = Long.parseLong(delayStr);
            } catch (NumberFormatException e) {
                Location location = reader.getLocation();
                InvalidValue error = new InvalidValue("Invalid delay value", location, e);
                context.addError(error);
            }
        }
        return delay;
    }

}
