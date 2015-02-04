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
package org.fabric3.monitor.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 *
 */
public class MonitorWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 9010394726444606704L;
    private URI classLoaderId;
    private Class<?> monitorType;
    private URI monitorable;
    private String destination;

    public MonitorWireTargetDefinition(Class<?> monitorType, URI monitorable, String destination) {
        this.monitorType = monitorType;
        this.monitorable = monitorable;
        this.destination = destination;
        setUri(null);
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public Class<?> getMonitorType() {
        return monitorType;
    }

    public URI getMonitorable() {
        return monitorable;
    }

    public String getDestination() {
        return destination;
    }
}
