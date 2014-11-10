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
 */
package org.fabric3.binding.web.runtime.channel;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fabric3.api.host.util.IOHelper;
import org.fabric3.binding.web.runtime.common.InvalidContentTypeException;

/**
 * Coordinates the RESTful pub/sub protocol for active channels. Incoming requests are routed through the Atmosphere gateway servlet to this
 * implementation. Requests are resolved to their target channel based on the servlet path. GET operations are subsequently routed to a {@link
 * ChannelSubscriber} for the channel and POST operations to a {@link ChannelPublisher}.
 */
public class ChannelRouter extends HttpServlet {
    private static final long serialVersionUID = -1830803509605261532L;
    private static final String ISO_8859_1 = "ISO-8859-1";

    private transient PubSubManager pubSubManager;
    private transient ChannelMonitor monitor;

    public ChannelRouter(PubSubManager pubSubManager, ChannelMonitor monitor) {
        this.pubSubManager = pubSubManager;
        this.monitor = monitor;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String info = request.getPathInfo();
        if (info == null) {
            response.setStatus(404);
            return;
        }
        String path = info.substring(1);    // strip leading '/'
        ChannelSubscriber subscriber = pubSubManager.getSubscriber(path);
        if (subscriber == null) {
            response.setStatus(404);
            return;
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
        String info = request.getPathInfo();
        if (info == null) {
            response.setStatus(404);
            return;
        }
        String path = info.substring(1);    // strip leading '/'
        ChannelPublisher publisher = pubSubManager.getPublisher(path);
        if (publisher == null) {
            response.setStatus(404);
            return;
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
        } catch (PublishException | IOException e) {
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
