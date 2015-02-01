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
package org.fabric3.fabric.host;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 * The default port allocator implementation. <p> Implements a brute-force port allocation approach by scanning a block of available ports and opening both TCP
 * and UDP sockets as a mechanism for maintaining port locks. After receiving a port, a client must invoke {@link Port#bind(Port.TYPE)} prior to binding a TCP
 * or UDP socket in order to free the underlying lock for the socket type. Note that the port will still hold a lock for the other socket type, which will
 * guarantee the port remains inaccessible to other clients if the owning client temporarily releases its socket connection (some transport bindings such as
 * ZeroMQ may close and re-establish socket connections over the life of a communication session).
 */
@Management(name = "PortAllocator", path = "/runtime/ports", group = "kernel", description = "Manages runtime ports")
public class PortAllocatorImpl implements PortAllocator {
    private int min = NOT_ALLOCATED;
    private int max = NOT_ALLOCATED;
    private String configuredHost;
    private Map<String, List<Port>> allocated = new HashMap<>();
    private LinkedList<Integer> unallocated = new LinkedList<>();

    @Property(required = false)
    @Source("$systemConfig/f3:runtime/@port.range")
    public void setRange(String range) {
        String[] tokens = range.split("-");
        if (tokens.length == 2) {
            // port range specified
            min = parsePortNumber(tokens[0]);
            max = parsePortNumber(tokens[1]);
        } else {
            throw new IllegalArgumentException("Invalid port range specified in the runtime system configuration");
        }
    }

    @Property(required = false)
    @Source("$systemConfig/f3:runtime/@host.address")
    public void setHost(String host) {
        configuredHost = host;
    }

    @Init
    public void init() throws Fabric3Exception {
        if (min == NOT_ALLOCATED && max == NOT_ALLOCATED) {
            return;
        }
        if (min > 0) {
            if (max < 0) {
                throw new IllegalArgumentException("Invalid maximum port value: " + max);
            }
        } else if (max > 0) {
            throw new IllegalArgumentException("Invalid minimum port value: " + min);
        }
        if (max < min) {
            throw new IllegalArgumentException("Maximum port cannot be less than minimum port");
        }
        for (int i = min; i <= max; i++) {
            unallocated.add(i);
        }
    }

    @Destroy
    public void destroy() {
        for (List<Port> ports : allocated.values()) {
            for (Port port : ports) {
                port.release();
            }
        }
    }

    @ManagementOperation(path = "/")
    public Map<String, List<Port>> getAllocatedPorts() {
        return allocated;
    }

    @ManagementOperation
    public List<Port> getAllocatedPorts(String type) {
        List<Port> ports = allocated.get(type);
        if (ports == null) {
            return Collections.emptyList();
        }
        return ports;
    }

    public boolean isPoolEnabled() {
        return min != NOT_ALLOCATED && max != NOT_ALLOCATED;
    }

    public Port allocate(String name, String type) throws Fabric3Exception {
        List<Port> ports = checkAllocated(name, type);
        while (true) {
            if (unallocated.isEmpty()) {
                throw new Fabric3Exception("No ports available");
            }
            int portNumber = unallocated.remove();
            SocketPair pair = checkAvailability(portNumber);
            if (pair != null) {
                Port port = new PortImpl(name, portNumber, pair.getServerSocket(), pair.getDatagramSocket());
                if (ports == null) {
                    ports = new ArrayList<>();
                    allocated.put(type, ports);
                }
                ports.add(port);
                return port;
            }
        }
    }

    public Port reserve(String name, String type, int portNumber) throws Fabric3Exception {
        List<Port> ports = checkAllocated(name, type);
        SocketPair pair = checkAvailability(portNumber);
        if (pair == null) {
            throw new Fabric3Exception("Port allocated: " + portNumber);
        }
        int pos = unallocated.indexOf(portNumber);
        if (pos >= 0) {
            unallocated.remove(pos);
        }

        Port port = new PortImpl(name, portNumber, pair.getServerSocket(), pair.getDatagramSocket());
        if (ports == null) {
            ports = new ArrayList<>();
            allocated.put(type, ports);
        }
        ports.add(port);
        return port;
    }

    public int getAllocatedPortNumber(String name) {
        for (List<Port> ports : allocated.values()) {
            for (Port port : ports) {
                if (port.getName().equals(name)) {
                    return port.getNumber();
                }
            }
        }
        return NOT_ALLOCATED;
    }

    public Set<String> getPortTypes() {
        return allocated.keySet();
    }

    public void release(int portNumber) {
        for (Iterator<Map.Entry<String, List<Port>>> iterator = allocated.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, List<Port>> entry = iterator.next();
            List<Port> ports = entry.getValue();
            for (Iterator<Port> portIterator = ports.iterator(); portIterator.hasNext(); ) {
                Port port = portIterator.next();
                if (port.getNumber() == portNumber) {
                    portIterator.remove();
                    unallocated.add(portNumber);
                    if (ports.isEmpty()) {
                        iterator.remove();
                    }
                    port.release();
                    return;
                }
            }
        }
    }

    public void release(String name) {
        for (Iterator<Map.Entry<String, List<Port>>> iterator = allocated.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, List<Port>> entry = iterator.next();
            final List<Port> ports = entry.getValue();
            for (Iterator<Port> portIterator = ports.iterator(); portIterator.hasNext(); ) {
                Port port = portIterator.next();
                if (port.getName().equals(name)) {
                    portIterator.remove();
                    unallocated.add(port.getNumber());
                    if (ports.isEmpty()) {
                        iterator.remove();
                    }
                    port.release();
                    return;
                }
            }
        }
    }

    public void releaseAll(String type) {
        List<Port> ports = allocated.remove(type);
        if (ports != null) {
            for (Port port : ports) {
                unallocated.add(port.getNumber());
                port.release();
            }
        }
    }

    private List<Port> checkAllocated(String name, String type) throws Fabric3Exception {
        List<Port> ports = allocated.get(type);
        if (ports != null) {
            for (Port port : ports) {
                if (port.getName().equals(name)) {
                    throw new Fabric3Exception("Port already allocated: " + type);
                }
            }
        }
        return ports;
    }

    private SocketPair checkAvailability(int port) throws Fabric3Exception {
        SocketPair pair = lockPort(port);
        // try the wildcard address first
        if (pair == null) {
            return null;
        }
        return pair;
    }

    private SocketPair lockPort(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket();

            InetAddress address = null;
            if (configuredHost != null) {
                address = InetAddress.getByName(configuredHost);
            }
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(socketAddress);
            DatagramSocket datagramSocket = new DatagramSocket(port, address);
            datagramSocket.setReuseAddress(true);
            return new SocketPair(serverSocket, datagramSocket);
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    private int parsePortNumber(String portVal) {
        try {
            int port = Integer.parseInt(portVal);
            if (port < 0) {
                throw new IllegalArgumentException("Invalid port range specified in the runtime system configuration: " + port);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port range specified in the runtime system configuration: ", e);
        }
    }

    private class SocketPair {
        private ServerSocket serverSocket;
        private DatagramSocket datagramSocket;

        private SocketPair(ServerSocket serverSocket, DatagramSocket datagramSocket) {
            this.serverSocket = serverSocket;
            this.datagramSocket = datagramSocket;
        }

        public ServerSocket getServerSocket() {
            return serverSocket;
        }

        public DatagramSocket getDatagramSocket() {
            return datagramSocket;
        }
    }

}
