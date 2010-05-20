/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.host.runtime;

import java.io.File;
import java.net.URI;

import org.w3c.dom.Document;

import org.fabric3.host.monitor.MonitorConfigurationException;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.stream.Source;

/**
 * Provides operations to bootstrap a runtime.
 *
 * @version $Revision$ $Date$
 */
public interface BootstrapService {

    /**
     * Loads the system configuration value from a systemConfig.xml file or creates a default value if the file does not exist.
     *
     * @param configDirectory the directory where the file is located
     * @return the loaded value
     * @throws ParseException if an error parsing the file contents is encountered
     */
    public Document loadSystemConfig(File configDirectory) throws ParseException;

    /**
     * Returns a configuration property value for the runtime domain from the given source.
     *
     * @param source the source to read
     * @return the domain configuration property
     * @throws ParseException if an error reading the source is encountered
     */
    Document loadSystemConfig(Source source) throws ParseException;

    /**
     * Creates a default configuration property value for the runtime domain.
     *
     * @return a document representing the configuration property
     */
    Document createDefaultSystemConfig();

    /**
     * Returns the configured domain name the runtime should join. If not configured, the default domain name will be returned.
     *
     * @param systemConfig the system configuration
     * @return the domain name
     * @throws ParseException if there is an error parsing the domain name
     */
    URI parseDomainName(Document systemConfig) throws ParseException;

    /**
     * Returns the configured JMX port range. If not configured, the default range (1199) will be returned.
     *
     * @param systemConfig the system configuration
     * @return the JMX port range
     * @throws ParseException if there is an error parsing the JMX port range
     */
    PortRange parseJmxPort(Document systemConfig) throws ParseException;

    /**
     * Creates a {@link MonitorEventDispatcher} for dispatching runtime events.
     *
     * @param systemConfig the runtime system configuration
     * @return the dispatcher
     * @throws MonitorConfigurationException if there is an error creating the dispatcher
     */
    MonitorEventDispatcher createMonitorDispatcher(Document systemConfig) throws MonitorConfigurationException;

    /**
     * Introspects the contents of a file system repository and categorizes its contents as extensions or user contributions.
     *
     * @param directory the repository directory
     * @return the result
     * @throws ScanException if an error occurs during the scan operation
     */
    ScanResult scanRepository(File directory) throws ScanException;

    /**
     * Instantiates a default runtime implementation.
     *
     * @param configuration the base configuration for the runtime
     * @return the runtime instance
     */
    Fabric3Runtime createDefaultRuntime(RuntimeConfiguration configuration);

    /**
     * Instantiates a RuntimeCoordinator.
     *
     * @param configuration the configuration for the coordinator
     * @return the coordinator instance
     */
    RuntimeCoordinator createCoordinator(BootConfiguration configuration);

}