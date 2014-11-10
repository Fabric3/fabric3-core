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
package org.fabric3.spi.federation.addressing;

import java.util.List;

/**
 * Provides a view of physical socket addresses associated with a logical endpoints in the domain.
 */
public interface AddressCache {

    /**
     * Returns a collections of active socket addresses for the endpoint in the domain.
     *
     * @param endpointId the endpoint id.
     * @return a collections of active socket addresses for the endpoint in the domain
     */
    List<SocketAddress> getActiveAddresses(String endpointId);

    /**
     * Publishes an address event.
     *
     * @param event the address event
     */
    void publish(AddressEvent event);

    /**
     * Subscribes a listener to receive notifications when a socket associated with the endpoint changes in the domain.
     *
     * @param endpointId the endpoint id
     * @param listener   the listener
     */
    void subscribe(String endpointId, AddressListener listener);

    /**
     * Un-subscribes a listener.
     *
     * @param endpointId the endpoint id
     * @param listenerId the listener id
     */
    void unsubscribe(String endpointId, String listenerId);

}
