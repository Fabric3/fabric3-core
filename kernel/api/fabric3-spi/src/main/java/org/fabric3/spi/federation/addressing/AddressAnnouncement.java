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

/**
 * Denotes a new socket bound on a runtime for an endpoint or the removal of an existing socket.
 */
public class AddressAnnouncement extends AddressEvent {
    private static final long serialVersionUID = 5338562626119315692L;
    public enum Type {
        ACTIVATED, REMOVED
    }

    private String endpointId;
    private Type type;
    private SocketAddress address;

    public AddressAnnouncement(String endpointId, Type type, SocketAddress address) {
        this.endpointId = endpointId;
        this.type = type;
        this.address = address;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public Type getType() {
        return type;
    }

    public SocketAddress getAddress() {
        return address;
    }
}
