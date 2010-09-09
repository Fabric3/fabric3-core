/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.web.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.host.util.IOHelper;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.EventWrapper;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.model.type.xsd.XSDType;

import static org.fabric3.binding.web.runtime.ContentTypes.APPLICATION_JSON;
import static org.fabric3.binding.web.runtime.ContentTypes.APPLICATION_XHTML_XML;
import static org.fabric3.binding.web.runtime.ContentTypes.APPLICATION_XML;

/**
 * Implements POST semantics for the publish/subscribe protocol, where data is sent as events to the channel.
 * <p/>
 * An event is read from the HTTP request body and stored as a string in an {@link EventWrapper}. XML (JAXB) and JSON are supported as content type
 * systems. It is the responsibility of consumers to deserialize the wrapper content into an expected Java type.
 *
 * @version $Rev$ $Date$
 */
public class ChannelPublisherImpl implements ChannelPublisher {
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final QName XSD_ANY = new QName(XSDType.XSD_NS, "anyType");
    private static final JsonType<Object> JSON = new JsonType<Object>(String.class, Object.class);
    private static final XSDType XML = new XSDType(String.class, XSD_ANY);

    private EventStreamHandler next;

    public void publish(HttpServletRequest request) throws OperationException {
        EventWrapper wrapper = wrapEvent(request);
        handle(wrapper);
    }

    public void handle(Object event) {
        // pass the object to the head stream handler
        next.handle(event);
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }

    private EventWrapper wrapEvent(HttpServletRequest request) throws OperationException {
        try {
            String contentType = request.getHeader("Content-Type");
            DataType<?> eventType;
            if ((contentType == null)) {
                throw new ServiceRuntimeException("No content type specified: " + contentType);
            } else if (contentType.contains(APPLICATION_XML)
                    || contentType.contains(APPLICATION_XHTML_XML)
                    || contentType.contains(ContentTypes.TEXT_XML)) {

                eventType = XML;
            } else if (contentType.contains(APPLICATION_JSON)) {
                eventType = JSON;
            } else {
                throw new ServiceRuntimeException("Unknown content type: " + contentType);
            }
            String content = read(request);
            return new EventWrapper(eventType, content);
        } catch (IOException e) {
            throw new OperationException(e);
        }
    }

    /**
     * Reads the body of an HTTP request using the set encoding and returns the contents as a string.
     *
     * @param request the HTTP request
     * @return the contents as a string
     * @throws IOException if an error occurs reading the contents
     */
    public String read(HttpServletRequest request) throws IOException {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = ISO_8859_1;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOHelper.copy(request.getInputStream(), outputStream);
        return outputStream.toString(encoding);
    }

}
