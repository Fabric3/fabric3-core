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
package org.fabric3.fabric.transport;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.transport.Transport;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.annotation.management.OperationType.DELETE;
import static org.fabric3.api.annotation.management.OperationType.POST;

/**
 *
 */
@EagerInit
@Management(path = "/runtime/transports", description = "Manages runtime binding transports")
public class TransportService {
    private TransportServiceMonitor monitor;
    private Map<String, Transport> transports = Collections.emptyMap();

    public TransportService(@Monitor TransportServiceMonitor monitor) {
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setTransports(Map<String, Transport> transports) {
        this.transports = transports;
    }

    @ManagementOperation(path = "/")
    public Collection<String> getTransports() {
        return transports.keySet();
    }

    @ManagementOperation(type = DELETE, description = "Suspend a transport from receiving requests")
    public void suspend(String name) {
        Transport transport = transports.get(name);
        if (transport == null) {
            monitor.transportNotFound(name);
            return;
        }
        transport.suspend();
    }

    @ManagementOperation(type = DELETE, description = "Suspend all transports from receiving requests")
    public void suspendAll() {
        transports.values().forEach(Transport::suspend);
    }

    @ManagementOperation(type = POST, description = "Resume receiving requests for a transport")
    public void resume(String name) {
        Transport transport = transports.get(name);
        if (transport == null) {
            monitor.transportNotFound(name);
            return;
        }
        transport.resume();
    }

    @ManagementOperation(type = POST, description = "Resume receiving requests for all transports")
    public void resumeAll() {
        transports.values().forEach(Transport::resume);
    }
}
