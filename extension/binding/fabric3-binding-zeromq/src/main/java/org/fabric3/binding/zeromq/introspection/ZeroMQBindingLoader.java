/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.zeromq.introspection;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Loads a <code>binding.zeromq</code> element in a composite.
 * 
 * @version $Rev$ $Date$
 */
@EagerInit
public class ZeroMQBindingLoader implements TypeLoader<ZeroMQBindingDefinition> {
    private final LoaderHelper       loaderHelper;
    private static final Set<String> ATTRIBUTES = new HashSet<String>();

    static {
        ATTRIBUTES.add("uri");
        ATTRIBUTES.add("host");
        ATTRIBUTES.add("port");
        ATTRIBUTES.add("name");
    }

    public ZeroMQBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public ZeroMQBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        validateAttributes(reader, context);

        ZeroMQMetadata metadata = null;

        String uriStr = reader.getAttributeValue(null, "uri");
        String bindingName = reader.getAttributeValue(null, "name");

        if (uriStr != null) {
            // Add code to actually parse the uri
            // will be implemented once the basic behavior is in place
        } else {
            metadata = new ZeroMQMetadata();
        }

        ZeroMQBindingDefinition definition = new ZeroMQBindingDefinition(bindingName, metadata);
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        // now start parsing the xml subtree and see what
        // elements and set the values inside the ZeroMQMetadata.

        String name;
        String value;
        test: while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if ("host".equalsIgnoreCase(name)) {
                        value = reader.getAttributeValue(null, "name");
                        if (value == null)
                            metadata.setHost("localhost");
                        else
                            metadata.setHost(value);
                    } else if ("port".equalsIgnoreCase(name)) {
                        value = reader.getAttributeValue(null, "number");
                        if (value == null) {
                            InvalidValue error = new InvalidValue("No port set for binding.zmq", reader);
                            context.addError(error);
                        }
                        try {
                            int p = Integer.parseInt(value);
                            metadata.setPort(p);
                        } catch (NumberFormatException e) {
                            InvalidValue error = new InvalidValue("The port :[" + value + "] is not a number", reader,
                                    e);
                            context.addError(error);
                        }
                    }
                    break;
                case END_ELEMENT:
                    name = reader.getName().getLocalPart();
                    if (name.equalsIgnoreCase("binding.zeromq")) {
                        // System.out.println("Done with parsing binding.zeromq");
                        break test;
                    }
            }
        }
        return definition;

        // URI uri = null;
        //
        // if (bindingName == null || uriStr == null) {
        // if (uriStr == null) {
        // MissingAttribute ma = new MissingAttribute(
        // "Missing Attribute [uri] for binding.zeromq", reader);
        // context.addError(ma);
        // }
        // if (bindingName == null) {
        // MissingAttribute ma = new MissingAttribute(
        // "Missing Attribute [name] for binding.zeromq", reader);
        // context.addError(ma);
        // }
        // return null;
        // }
        //
        // try {
        // uri = new URI(uriStr);
        // } catch (URISyntaxException e) {
        // InvalidValue failure = new InvalidValue("Invalid URI :" + uriStr
        // + " for binding.zeromq", reader, e);
        // context.addError(failure);
        // return null;
        // }

        // ZeroMQBindingDefinition definition = new ZeroMQBindingDefinition(
        // bindingName, uri);
        //
        // loaderHelper.loadPolicySetsAndIntents(definition, reader, context);
        // LoaderUtil.skipToEndElement(reader);
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.contains(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }
}
