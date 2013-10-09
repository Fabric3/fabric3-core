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
package org.fabric3.binding.zeromq.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.zeromq.common.SocketAddressDefinition;
import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a <code>binding.zeromq</code> element in a composite.
 */
@EagerInit
public class ZeroMQBindingLoader extends AbstractValidatingTypeLoader<ZeroMQBindingDefinition> {
    private final LoaderHelper loaderHelper;

    public ZeroMQBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        addAttributes("name",
                      "requires",
                      "policySets",
                      "timeout",
                      "target",
                      "addresses",
                      "name",
                      "high.water",
                      "multicast.rate",
                      "multicast.recovery",
                      "send.buffer",
                      "receive.buffer",
                      "wireFormat");
    }

    public ZeroMQBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();

        String bindingName = reader.getAttributeValue(null, "name");

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        ZeroMQBindingDefinition definition = new ZeroMQBindingDefinition(bindingName, metadata);

        String target = reader.getAttributeValue(null, "target");
        if (target != null) {
            try {
                URI targetUri = new URI(target);
                definition.setTargetUri(targetUri);
            } catch (URISyntaxException e) {
                InvalidValue error = new InvalidValue("Invalid target URI specified: " + target, startLocation, e);
                context.addError(error);
            }
        }

        String timeoutStr = reader.getAttributeValue(null, "timeout");
        if (timeoutStr != null) {
            try {
                long timeout = Long.parseLong(timeoutStr);
                metadata.setTimeout(timeout);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("Invalid timeout specified: " + timeoutStr, startLocation, e);
                context.addError(error);
            }
        }
        String addresses = reader.getAttributeValue(null, "addresses");
        long highWater = parseLong("high.water", reader, context);
        long multicastRate = parseLong("multicast.rate", reader, context);
        long multicastRecovery = parseLong("multicast.recovery", reader, context);
        long sendBuffer = parseLong("send.buffer", reader, context);
        long receiveBuffer = parseLong("receive.buffer", reader, context);
        String wireFormat = reader.getAttributeValue(null, "wireFormat");

        if (addresses != null) {
            List<SocketAddressDefinition> addressDefinitions = new ArrayList<SocketAddressDefinition>();
            String[] addressStrings = addresses.split("\\s+");
            for (String entry : addressStrings) {
                String[] tokens = entry.split(":");
                if (tokens.length != 2) {
                    context.addError(new InvalidValue("Invalid address: " + entry, startLocation, definition));
                } else {
                    try {
                        String host = tokens[0];
                        int port = Integer.parseInt(tokens[1]);
                        addressDefinitions.add(new SocketAddressDefinition(host, port));
                    } catch (NumberFormatException e) {
                        context.addError(new InvalidValue("Invalid port: " + e.getMessage(), startLocation, definition));
                    }
                }
            }
            metadata.setSocketAddresses(addressDefinitions);
        }

        metadata.setHighWater(highWater);
        metadata.setMulticastRate(multicastRate);
        metadata.setMulticastRecovery(multicastRecovery);
        metadata.setSendBuffer(sendBuffer);
        metadata.setReceiveBuffer(receiveBuffer);
        metadata.setWireFormat(wireFormat);

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);

        validateAttributes(reader, context, definition);

        LoaderUtil.skipToEndElement(reader);
        return definition;
    }

    private long parseLong(String name, XMLStreamReader reader, IntrospectionContext context) {
        try {
            String val = reader.getAttributeValue(null, name);
            if (val == null) {
                return -1;
            }
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            Location location = reader.getLocation();
            InvalidValue error = new InvalidValue("Invalid value specified for " + name, location, e);
            context.addError(error);
            return -1;
        }
    }

}
