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
package org.fabric3.runtime.tomcat.connector;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.fabric3.api.host.ContainerException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class ConnectorServiceImpl implements ConnectorService {
    private int defaultHttpPort = 8080;   // default Tomcat port
    private Service service;
    private Connector defaultHttpConnector;


    public ConnectorServiceImpl(@Reference Service service) {
        this.service = service;
    }

    @Property(required = false)
    public void setHttpPort(int defaultHttpPort) {
        this.defaultHttpPort = defaultHttpPort;
    }

    @Init
    public void init() throws ContainerException {
        for (Connector connector : service.findConnectors()) {
            if (connector.getPort() == defaultHttpPort) {
                defaultHttpConnector = connector;
                break;
            }
        }
        if (defaultHttpConnector == null) {
            throw new ContainerException("Default HTTP connector not found for port: " + defaultHttpPort
                    + ". Ensure that the Fabric3 runtime HTTP port is configured in systemConfig.xml.");
        }
    }

    public Connector getConnector() {
        return defaultHttpConnector;
    }
}
