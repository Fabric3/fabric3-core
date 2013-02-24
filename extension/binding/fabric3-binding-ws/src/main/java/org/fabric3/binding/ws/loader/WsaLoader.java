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
 *
 */
package org.fabric3.binding.ws.loader;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.binding.ws.model.EndpointReference;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.EagerInit;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 */
@EagerInit
public class WsaLoader implements TypeLoader<EndpointReference> {
    private static final QName ENDPOINT_REFERENCE = new QName("http://www.w3.org/2005/08/addressing", "EndpointReference");
    private static final QName ADDRESS = new QName("http://www.w3.org/2005/08/addressing", "Address");

    public EndpointReference load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        EndpointReference endpointReference = null;
        Location location = reader.getLocation();
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (ADDRESS.equals(reader.getName())) {
                        String addressStr = reader.getElementText();
                        try {
                            URI uri = new URI(addressStr);
                            return new EndpointReference(uri);
                        } catch (URISyntaxException e) {
                            InvalidValue error = new InvalidValue("Invalid address", location);
                            context.addError(error);
                            return new EndpointReference(URI.create("http://errornoaddress"));
                        }
                    }
                    break;

                case END_ELEMENT:
                    if (ADDRESS.equals(reader.getName())) {
                        if (endpointReference == null) {
                            MissingAddress error = new MissingAddress("Address not specified", location);
                            context.addError(error);
                            return new EndpointReference(URI.create("http://errornoaddress"));
                        }
                        return endpointReference;
                    }
            }
        }
    }
}
