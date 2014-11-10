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

import javax.servlet.http.HttpServletRequest;

import org.fabric3.spi.container.channel.ChannelConnection;

/**
 * Receives incoming requests for a channel published as a web endpoint. This dispatcher implements GET semantics for the RESTful publish/subscribe
 * protocol, where clients are subscribed to receive events from the channel. A successful GET will either result in the creation of a websocket
 * connection for clients that support it, or a suspended comet connection. Subsequent events published to the channel will be pushed to all
 * subscribed clients.
 */
public interface ChannelSubscriber extends ChannelConnection {

    /**
     * Perform the subscription
     *
     * @param request the HTTP request
     * @throws PublishException if an error occurs subscribing
     */
    void subscribe(HttpServletRequest request) throws PublishException;
}
