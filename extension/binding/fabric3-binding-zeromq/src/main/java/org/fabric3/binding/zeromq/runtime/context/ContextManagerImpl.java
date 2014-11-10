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
        controlSocket.setLinger(0);
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
        loadLibrary(hostInfo);

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
     * Uses the OperatingSystem information of the HostInfo to decide what library to load. On Windows the library name is "libzmq". On Linux the library
     * name is "zmq".
     *
     * @param hostInfo Based on the OperatingSystem member the needed Library will be loaded.
     */
    protected void loadLibrary(HostInfo hostInfo) {
        if (hostInfo == null) {
            return;
        }
        // don't load native lib for jeromq profile
        if (getClass().getClassLoader().getResource("org/codehaus/fabric3/jeromq") != null) {
            return;
        }
        String osName = hostInfo.getOperatingSystem().getName().toLowerCase();
        if (osName == null) {
            return;
        }

        for (ZMQLibraryInitializer lib : ZMQLibraryInitializer.values()) {
            if (osName.toLowerCase().contains(lib.name().toLowerCase())) {
                lib.loadLibrary();
                return;
            }
        }
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

        private void loadLibrary() {
            if (!this.equals(OTHER)) {
                System.loadLibrary(libName);
            }
        }
    }
}
