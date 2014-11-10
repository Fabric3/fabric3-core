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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.ws.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.binding.ws.model.EndpointReference;
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
