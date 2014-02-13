/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.context;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.fabric3.api.host.runtime.HostInfo;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

/**
 *
 */
@EagerInit
public class ContextManagerImpl implements ContextManager {
    private static final byte[] EMPTY_BYTES = new byte[0];

    private Context context;
    private ZMQ.Socket controlSocket;

    // client leases
    private Set<String> leases = new ConcurrentSkipListSet<>();

    // object monitor used as a sync for closing the ZeroMQ context after all open sockets have been closed
    private final Object termMonitor = new Object();

    @Reference
    protected HostInfo hostInfo;

    public Context getContext() {
        return context;
    }

    public ZMQ.Socket createControlSocket() {
        ZMQ.Socket controlSocket = context.socket(ZMQ.SUB);
        controlSocket.subscribe(EMPTY_BYTES);
        controlSocket.connect("inproc://fabric3");
        return controlSocket;
    }

    public void reserve(String id) {
        leases.add(id);
    }

    public void release(String id) {
        synchronized (termMonitor) {
            leases.remove(id);
            termMonitor.notifyAll();
        }
    }

    @Init
    public void init() {
        // Windows requires the ZMQ library to be loaded as the JZMQ library is linked to it and Windows is unable to resolve it relative to the JZMQ library
        // System.loadLibrary("zmq");
        ZMQLibraryInitializer.loadLibrary(hostInfo);

        context = ZMQ.context(1);

        controlSocket = context.socket(ZMQ.PUB);
        controlSocket.bind("inproc://fabric3");
    }

    /**
     * Closes the ZeroMQ context after all socket clients have released their locks. This guarantees the context will not be closed prior to all sockets being
     * closed. Note that ZeroMQ requires sockets to be open, accessed and closed by the same thread.
     */
    @Destroy
    public void destroy() {
        if (controlSocket != null) {
            // send message for all listening sockets to close
            controlSocket.send(new byte[0], 0);
        }
        while (!leases.isEmpty()) {
            synchronized (termMonitor) {
                if (leases.isEmpty()) {
                    break;
                }
                try {
                    termMonitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (controlSocket != null) {
            controlSocket.close();
        }
        context.term();
    }

    /**
     * Initializes the ZeroMQ library on Windows and Linux. If the ZeroMQ Library is not initialized before the Context is created the loading of the library is
     * delegated to the Operating System. This causes problems since F3 can't control where to load the libraries from. To work around this problem we
     * initialize ZeroMQ base library prior to JZMQ (which happens when a Context is created).
     */
    protected enum ZMQLibraryInitializer {
        WINDOWS("libzmq-v120-mt-3_2_4"), LINUX("zmq"), OTHER("");

        private String libName;

        private ZMQLibraryInitializer(String libName) {
            this.libName = libName;
        }

        /**
         * Uses the OperatingSystem information of the HostInfo to decide what library to load. On Windows the library name is "libzmq". On Linux the library
         * name is "zmq".
         *
         * @param hostInfo Based on the OperatingSystem member the needed Library will be loaded.
         */
        public static void loadLibrary(HostInfo hostInfo) {
            if (hostInfo == null) {
                return;
            }
            String osName = hostInfo.getOperatingSystem().getName().toLowerCase();
            if (osName == null) {
                return;
            }

            for (ZMQLibraryInitializer lib : values()) {
                if (osName.toLowerCase().contains(lib.name().toLowerCase())) {
                    lib.loadLibrary();
                    return;
                }
            }
        }

        private void loadLibrary() {
            if (!this.equals(OTHER)) {
                System.loadLibrary(libName);
            }
        }
    }
}
