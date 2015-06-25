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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.introspection.xml.common.AbstractExtensibleTypeLoader;
import org.fabric3.introspection.xml.common.BindingHelper;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ChannelTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loads a channel definition from an XML-based assembly file
 */
@EagerInit
public class ChannelLoader extends AbstractExtensibleTypeLoader<Channel> {

    private static final QName CHANNEL = new QName(SCA_NS, "channel");

    private Map<String, ChannelTypeLoader> channelTypeLoaders = Collections.emptyMap();

    public ChannelLoader(@Reference LoaderRegistry registry) {
        super(registry);
        addAttributes("name", "requires", "type", "local");
    }

    @Reference(required = false)
    public void setChannelTypeLoaders(Map<String, ChannelTypeLoader> channelTypeLoaders) {
        this.channelTypeLoaders = channelTypeLoaders;
        for (ChannelTypeLoader channelTypeLoader : channelTypeLoaders.values()) {
            addAttributes(channelTypeLoader.getAttributes());
        }
    }

    public Channel load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Component name not specified", startLocation);
            context.addError(failure);
            return null;
        }

        String channelType = reader.getAttributeValue(null, "type");
        if (channelType == null) {
            channelType = Channel.DEFAULT_TYPE;
        }

        boolean local = Boolean.parseBoolean(reader.getAttributeValue(null, "local"));

        Channel definition = new Channel(name, channelType, local);
        definition.setContributionUri(context.getContributionUri());
        validateAttributes(reader, context, definition);

        ChannelTypeLoader channelTypeLoader = channelTypeLoaders.get(channelType);
        if (channelTypeLoader == null) {
            context.addError(new InvalidValue("Invalid channel type", startLocation, definition));
        } else {
            channelTypeLoader.load(definition, reader, context);
        }

        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    Location location = reader.getLocation();

                    QName elementName = reader.getName();
                    ModelObject type = registry.load(reader, ModelObject.class, context);
                    if (type instanceof Binding) {
                        if (local) {
                            context.addError(new IllegalBinding("Bindings cannot be configured on a local channel: " + name, location, definition));
                            continue;
                        }
                        Binding binding = (Binding) type;
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