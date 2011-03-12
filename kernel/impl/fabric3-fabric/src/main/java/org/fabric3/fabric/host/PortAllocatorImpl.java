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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;

/**
 * @version $Rev: 10029 $ $Date: 2011-02-21 16:56:40 -0500 (Mon, 21 Feb 2011) $
 */
@Management(name = "PortAllocator", path = "/runtime/ports", group = "kernel", description = "Manages runtime ports")
public class PortAllocatorImpl implements PortAllocator {
    private int min = NOT_ALLOCATED;
    private int max = NOT_ALLOCATED;
    private Map<String, Integer> allocated = new HashMap<String, Integer>();
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

    @ManagementOperation(path = "/")
    public Map<String, Integer> getAllocatedPorts() {
        return allocated;
    }

    @ManagementOperation
    public Integer getAllocatedPorts(String type) {
        return allocated.get(type);
    }

    public boolean isPoolEnabled() {
        return min != NOT_ALLOCATED && max != NOT_ALLOCATED;
    }

    public int allocate(String type) throws PortAllocationException {
        if (allocated.containsKey(type)) {
            throw new PortTypeAllocatedException(type);
        }
        while (true) {
            if (unallocated.isEmpty()) {
                throw new PortAllocationException("No ports available");
            }
            int port = unallocated.remove();
            if (checkAvailability(port)) {
                allocated.put(type, port);
                return port;
            }
        }
    }

    public void reserve(String type, int port) throws PortAllocationException {
        if (allocated.containsKey(type)) {
            throw new PortTypeAllocatedException(type);
        }
        if (!checkAvailability(port)) {
            throw new PortAllocatedException(port);
        }
        int pos = unallocated.indexOf(port);
        if (pos >= 0) {
            unallocated.remove(pos);
        }
        allocated.put(type, port);
    }

    public int getAllocatedPort(String type) {
        Integer port = allocated.get(type);
        if (port == null) {
            return NOT_ALLOCATED;
        }
        return port;
    }

    public void release(String type) {
        Integer port = allocated.remove(type);
        if (port != null && (port > min && port < max)) {
            unallocated.add(port);
        }
    }

    private boolean checkAvailability(int port) throws PortAllocationException {

        try {
            // try the wildcard address first
            if (!checkPortOnHost(null, port)) {
                return false;
            }
            String localhost = InetAddress.getLocalHost().getCanonicalHostName();
            InetAddress[] addresses = InetAddress.getAllByName(localhost);
            for (InetAddress address : addresses) {
                if (!checkPortOnHost(address, port)) {
                    return false;
                }
            }
            return true;
        } catch (UnknownHostException e) {
            throw new PortAllocationException(e);
        }
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
