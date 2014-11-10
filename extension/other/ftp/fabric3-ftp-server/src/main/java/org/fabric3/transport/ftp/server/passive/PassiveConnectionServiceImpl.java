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
package org.fabric3.transport.ftp.server.passive;

import java.util.Stack;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 *
 */
@EagerInit
public class PassiveConnectionServiceImpl implements PassiveConnectionService {

    private int minPort = 6000;
    private int maxPort = 7000;
    private Stack<Integer> ports = new Stack<>();

    /**
     * Sets the minimum passive port.
     *
     * @param minPort Minimum passive port.
     */
    @Property(required = false)
    public void setMinPort(int minPort) {
        this.minPort = minPort;
    }

    /**
     * Sets the maximum passive port.
     *
     * @param maxPort Maximum passive port.
     */
    @Property(required = false)
    public void setMaxPort(int maxPort) {
        this.maxPort = maxPort;
    }

    /**
     * Initializes the port.
     */
    @Init
    public void init() {
        for (int i = minPort; i <= maxPort; i++) {
            ports.push(i);
        }
    }

    /**
     * Acquires the next available pasive port.
     *
     * @return Next available passive port.
     * @throws InterruptedException
     */
    public synchronized int acquire() throws InterruptedException {
        while (ports.empty()) {
            wait();
        }
        return ports.pop();
    }

    /**
     * Release a passive port.
     *
     * @param port Port to be released.
     */
    public synchronized void release(int port) {
        ports.push(port);
        notifyAll();
    }

}
