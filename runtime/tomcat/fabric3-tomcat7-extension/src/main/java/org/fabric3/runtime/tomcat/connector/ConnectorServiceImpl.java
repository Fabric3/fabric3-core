/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
*/
package org.fabric3.runtime.tomcat.connector;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
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
    public void init() throws ConnectorInitException {
        for (Connector connector : service.findConnectors()) {
            if (connector.getPort() == defaultHttpPort) {
                defaultHttpConnector = connector;
                break;
            }
        }
        if (defaultHttpConnector == null) {
            throw new ConnectorInitException("Default HTTP connector not found for port: " + defaultHttpPort
                    + ". Ensure that the Fabric3 runtime HTTP port is configured in systemConfig.xml.");
        }
    }

    public Connector getConnector() {
        return defaultHttpConnector;
    }
}
