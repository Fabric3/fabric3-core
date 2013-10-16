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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.introspection.xml.common.BindingHelper;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.spi.introspection.xml.ChannelTypeLoader;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a channel definition from an XML-based assembly file
 */
@EagerInit
public class ChannelLoader extends AbstractExtensibleTypeLoader<ChannelDefinition> {

    private static final QName CHANNEL = new QName(SCA_NS, "channel");

    private LoaderHelper loaderHelper;
    private boolean roundTrip;

    private Map<String, ChannelTypeLoader> channelTypeLoaders = Collections.emptyMap();

    public ChannelLoader(@Reference LoaderRegistry registry, @Reference LoaderHelper loaderHelper) {
        super(registry);
        addAttributes("name", "requires", "type");
        this.loaderHelper = loaderHelper;
    }

    @Property(required = false)
    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    @Reference(required = false)
    public void setChannelTypeLoaders(Map<String, ChannelTypeLoader> channelTypeLoaders) {
        this.channelTypeLoaders = channelTypeLoaders;
        for (ChannelTypeLoader channelTypeLoader : channelTypeLoaders.values()) {
            addAttributes(channelTypeLoader.getAttributes());
        }
    }

    public ChannelDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Component name not specified", startLocation);
            context.addError(failure);
            return null;
        }

        URI uri = context.getContributionUri();
        String channelType = reader.getAttributeValue(null, "type");
        if (channelType == null) {
            channelType = ChannelDefinition.DEFAULT_TYPE;
        }

        ChannelDefinition definition = new ChannelDefinition(name, uri, channelType);

        validateAttributes(reader, context, definition);

        if (roundTrip) {
            definition.enableRoundTrip();
        }

        ChannelTypeLoader channelTypeLoader = channelTypeLoaders.get(channelType);
        if (channelTypeLoader == null) {
            context.addError(new InvalidValue("Invalid channel type", startLocation, definition));
        } else {
            channelTypeLoader.load(definition, reader, context);
        }

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    Location location = reader.getLocation();

                    QName elementName = reader.getName();
                    ModelObject type = registry.load(reader, ModelObject.class, context);
                    if (type instanceof BindingDefinition) {
                        BindingDefinition binding = (BindingDefinition) type;
                        boolean check = BindingHelper.checkDuplicateNames(binding, definition.getBindings(), location, context);
                        if (check) {
                            definition.addBinding(binding);
                        }
                    } else if (type == null) {
                        // no type, continue processing
                        continue;
                    } else {
                        context.addError(new UnrecognizedElement(reader, location, definition));
                        continue;
                    }
                    if (!reader.getName().equals(elementName) || reader.getEventType() != END_ELEMENT) {
                        throw new AssertionError("Loader must position the cursor to the end element");
                    }
                case END_ELEMENT:
                    elementName = reader.getName();
                    if (CHANNEL.equals(elementName)) {
                        return definition;
                    }
            }
        }
    }

    public QName getXMLType() {
        return CHANNEL;
    }

}