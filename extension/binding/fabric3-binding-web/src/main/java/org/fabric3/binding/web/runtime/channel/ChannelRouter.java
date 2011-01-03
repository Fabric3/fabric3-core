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
package org.fabric3.binding.web.runtime.channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.binding.web.runtime.common.InvalidContentTypeException;
import org.fabric3.host.util.IOHelper;
import org.fabric3.spi.channel.EventWrapper;

/**
 * Coordinates the RESTful pub/sub protocol for active channels. Incoming requests are routed through the Atmosphere gateway servlet to this
 * implementation. Requests are resolved to their target channel based on the servlet path. GET operations are subsequently routed to a {@link
 * ChannelSubscriber} for the channel and POST operations to a {@link ChannelPublisher}.
 *
 * @version $Rev$ $Date$
 */
public class ChannelRouter extends HttpServlet {
    private static final long serialVersionUID = -1830803509605261532L;
    private static final String ISO_8859_1 = "ISO-8859-1";

    private PubSubManager pubSubManager;
    private ChannelMonitor monitor;

    public ChannelRouter(PubSubManager pubSubManager, ChannelMonitor monitor) {
        this.pubSubManager = pubSubManager;
        this.monitor = monitor;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo().substring(1);    // strip leading '/'
        ChannelSubscriber subscriber = pubSubManager.getSubscriber(path);
        if (subscriber == null) {
            // TODO return 404
            throw new AssertionError("Path not found: " + path);
        }
        try {
            subscriber.subscribe(request);
        } catch (PublishDeniedException e) {
            response.setStatus(403);   // forbidden
        } catch (PublishException e) {
            response.setStatus(500);
            monitor.error(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getPathInfo().substring(1);    // strip leading '/'
        ChannelPublisher publisher = pubSubManager.getPublisher(path);
        if (publisher == null) {
            // TODO return 404
            throw new AssertionError("Path not found");
        }
        try {
            String contentType = request.getHeader("Content-Type");

            String encoding = request.getCharacterEncoding();
            if (encoding == null) {
                encoding = ISO_8859_1;
            }
            ServletInputStream stream = request.getInputStream();
            String data = read(stream, encoding);
            EventWrapper wrapper = ChannelUtils.createWrapper(contentType, data);
            publisher.publish(wrapper);
        } catch (PublishDeniedException e) {
            response.setStatus(403);   // forbidden
        } catch (PublishException e) {
            response.setStatus(500);
            monitor.error(e);
        } catch (IOException e) {
            response.setStatus(500);
            monitor.error(e);
        } catch (InvalidContentTypeException e) {
            response.setStatus(400);
            monitor.error(e);
        }
    }

    /**
     * Reads the body of an HTTP request using the set encoding and returns the contents as a string.
     *
     * @param stream   the contents as a stream
     * @param encoding the content encoding
     * @return the contents as a string
     * @throws IOException if an error occurs reading the contents
     */
    private String read(InputStream stream, String encoding) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOHelper.copy(stream, outputStream);
        return outputStream.toString(encoding);
    }


}
