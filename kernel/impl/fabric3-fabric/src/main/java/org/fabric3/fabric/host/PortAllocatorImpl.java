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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.host;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;

/**
 * @version $Rev: 10029 $ $Date: 2011-02-21 16:56:40 -0500 (Mon, 21 Feb 2011) $
 */
@Management(name = "PortAllocator", path = "/runtime/ports", group = "kernel", description = "Manages runtime ports")
public class PortAllocatorImpl implements PortAllocator {
    private int min = NOT_ALLOCATED;
    private int max = NOT_ALLOCATED;
    private Map<String, List<Port>> allocated = new HashMap<String, List<Port>>();
    private LinkedList<Integer> unallocated = new LinkedList<Integer>();

    @Property(required = false)
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

    @Init
    public void init() throws PortAllocationException {
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
                port.releaseLock();
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

    public Port allocate(String name, String type) throws PortAllocationException {
        List<Port> ports = checkAllocated(name, type);
        while (true) {
            if (unallocated.isEmpty()) {
                throw new PortAllocationException("No ports available");
            }
            int portNumber = unallocated.remove();
            ServerSocket socket = checkAvailability(portNumber);
            if (socket != null) {
                Port port = new PortImpl(name, portNumber, socket);
                if (ports == null) {
                    ports = new ArrayList<Port>();
                    allocated.put(type, ports);
                }
                ports.add(port);
                return port;
            }
        }
    }

    public Port reserve(String name, String type, int portNumber) throws PortAllocationException {
        List<Port> ports = checkAllocated(name, type);
        ServerSocket socket = checkAvailability(portNumber);
        if (socket == null) {
            throw new PortAllocatedException(portNumber);
        }
        int pos = unallocated.indexOf(portNumber);
        if (pos >= 0) {
            unallocated.remove(pos);
        }

        Port port = new PortImpl(name, portNumber, socket);
        if (ports == null) {
            ports = new ArrayList<Port>();
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
        for (Iterator<Map.Entry<String, List<Port>>> iterator = allocated.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, List<Port>> entry = iterator.next();
            List<Port> ports = entry.getValue();
            for (Iterator<Port> portIterator = ports.iterator(); portIterator.hasNext();) {
                Port port = portIterator.next();
                if (port.getNumber() == portNumber) {
                    portIterator.remove();
                    unallocated.add(portNumber);
                    if (ports.isEmpty()) {
                        iterator.remove();
                    }
                    port.releaseLock();
                    return;
                }
            }

        }
    }

    public void release(String name) {
        for (Iterator<Map.Entry<String, List<Port>>> iterator = allocated.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, List<Port>> entry = iterator.next();
            final List<Port> ports = entry.getValue();
            for (Iterator<Port> portIterator = ports.iterator(); portIterator.hasNext();) {
                Port port = portIterator.next();
                if (port.getName().equals(name)) {
                    portIterator.remove();
                    unallocated.add(port.getNumber());
                    if (ports.isEmpty()) {
                        iterator.remove();
                    }
                    port.releaseLock();
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
                port.releaseLock();
            }
        }
    }

    private List<Port> checkAllocated(String name, String type) throws PortNameAllocatedException {
        List<Port> ports = allocated.get(type);
        if (ports != null) {
            for (Port port : ports) {
                if (port.getName().equals(name)) {
                    throw new PortNameAllocatedException(type);
                }
            }
        }
        return ports;
    }

    private ServerSocket checkAvailability(int port) throws PortAllocationException {

        //try {
            ServerSocket socket = lockPort(port);
            // try the wildcard address first
            if (socket == null) {
                return null;
            }
//            String localhost = InetAddress.getLocalHost().getCanonicalHostName();
//            InetAddress[] addresses = InetAddress.getAllByName(localhost);
//            for (InetAddress address : addresses) {
//            	if(address.isLoopbackAddress())
//            		continue;
//                if (!checkPortOnHost(address, port)) {
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }
//            }
            return socket;
//        } catch (UnknownHostException e) {
//            throw new PortAllocationException(e);
//        }
    }

    private ServerSocket lockPort(int port) {
        ServerSocket serverSocket;
        DatagramSocket datagramSocket = null;
        try {
            serverSocket = new ServerSocket();
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            serverSocket.setReuseAddress(true);
            serverSocket.bind(socketAddress);
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setReuseAddress(true);
            return serverSocket;
        } catch (IOException e) {
            // ignore
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
        return null;
    }

    private boolean checkPortOnHost(InetAddress address, int port) {
        ServerSocket serverSocket = null;
        DatagramSocket datagramSocket = null;
        try {
            serverSocket = new ServerSocket();
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            serverSocket.bind(socketAddress);
            datagramSocket = new DatagramSocket(port, address);
            datagramSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // ignore
        } finally {
            close(serverSocket);
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
        return false;
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

    private void close(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
