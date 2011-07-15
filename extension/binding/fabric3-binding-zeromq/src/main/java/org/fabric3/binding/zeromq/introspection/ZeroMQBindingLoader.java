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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loads a <code>binding.zeromq</code> element in a composite.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ZeroMQBindingLoader implements TypeLoader<ZeroMQBindingDefinition> {
    private final LoaderHelper loaderHelper;
    private static final Set<String> ATTRIBUTES = new HashSet<String>();

    static {
        ATTRIBUTES.add("target");
        ATTRIBUTES.add("host");
        ATTRIBUTES.add("name");
        ATTRIBUTES.add("high.water");
        ATTRIBUTES.add("multicast.rate");
        ATTRIBUTES.add("multicast.recovery");
        ATTRIBUTES.add("send.buffer");
        ATTRIBUTES.add("receive.buffer");
    }

    public ZeroMQBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public ZeroMQBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        validateAttributes(reader, context);

        String bindingName = reader.getAttributeValue(null, "name");

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        ZeroMQBindingDefinition definition = new ZeroMQBindingDefinition(bindingName, metadata);

        String target = reader.getAttributeValue(null, "target");
        if (target != null) {
            try {
                definition.setTargetUri(new URI(target));
            } catch (URISyntaxException e) {
                InvalidValue error = new InvalidValue("Invalid target URI specified: " + target, reader, e);
                context.addError(error);
            }
        }

        String host = reader.getAttributeValue(null, "host");
        long highWater = parseLong("high.water", reader, context);
        long multicastRate = parseLong("multicast.rate", reader, context);
        long multicastRecovery = parseLong("multicast.recovery", reader, context);
        long sendBuffer = parseLong("send.buffer", reader, context);
        long receiveBuffer = parseLong("receive.buffer", reader, context);

        metadata.setHost(host);
        metadata.setHighWater(highWater);
        metadata.setMulticastRate(multicastRate);
        metadata.setMulticastRecovery(multicastRecovery);
        metadata.setSendBuffer(sendBuffer);
        metadata.setReceiveBuffer(receiveBuffer);

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);
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
            InvalidValue error = new InvalidValue("Invalid value specified for " + name, reader, e);
            context.addError(error);
            return -1;
        }
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
