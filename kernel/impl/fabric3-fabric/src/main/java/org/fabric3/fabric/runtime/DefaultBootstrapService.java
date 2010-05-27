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
package org.fabric3.fabric.runtime;

import java.io.File;
import java.net.URI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.fabric.runtime.bootstrap.RepositoryScanner;
import org.fabric3.fabric.runtime.bootstrap.SystemConfigLoader;
import org.fabric3.host.monitor.MonitorConfigurationException;
import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapService;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.ParseException;
import org.fabric3.host.runtime.PortRange;
import org.fabric3.host.runtime.RuntimeConfiguration;
import org.fabric3.host.runtime.RuntimeCoordinator;
import org.fabric3.host.runtime.ScanException;
import org.fabric3.host.runtime.ScanResult;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.stream.Source;
import org.fabric3.host.RuntimeMode;
import org.fabric3.monitor.runtime.LogbackDispatcher;

/**
 * Default BootstrapFactory implementation.
 *
 * @version $Revision$ $Date$
 */
public class DefaultBootstrapService implements BootstrapService {
    private RepositoryScanner scanner;
    private SystemConfigLoader systemConfigLoader;

    public DefaultBootstrapService() {
        scanner = new RepositoryScanner();
        systemConfigLoader = new SystemConfigLoader();
    }

    public Document loadSystemConfig(File configDirectory) throws ParseException {
        return systemConfigLoader.loadSystemConfig(configDirectory);
    }


    public Document loadSystemConfig(Source source) throws ParseException {
        return systemConfigLoader.loadSystemConfig(source);
    }

    public Document createDefaultSystemConfig() {
        return systemConfigLoader.createDefaultSystemConfig();
    }

    public URI parseDomainName(Document systemConfig) throws ParseException {
        return systemConfigLoader.parseDomainName(systemConfig);
    }

    public RuntimeMode parseRuntimeMode(Document systemConfig) throws ParseException {
        return systemConfigLoader.parseRuntimeMode(systemConfig);
    }

    public PortRange parseJmxPort(Document systemConfig) throws ParseException {
        return systemConfigLoader.parseJmxPort(systemConfig);
    }

    public MonitorEventDispatcher createMonitorDispatcher(String elementName, Document systemConfig) throws MonitorConfigurationException {
        LogbackDispatcher dispatcher = new LogbackDispatcher(elementName);
        Element element = systemConfigLoader.getMonitorConfiguration(elementName, systemConfig);
        if (element != null) {
            dispatcher.configure(element);
        }
        return dispatcher;
    }

    public ScanResult scanRepository(HostInfo info) throws ScanException {
        return scanner.scan(info);
    }

    public Fabric3Runtime createDefaultRuntime(RuntimeConfiguration configuration) {
        return new DefaultRuntime(configuration);
    }

    public RuntimeCoordinator createCoordinator(BootConfiguration configuration) {
        return new DefaultCoordinator(configuration);
    }

}