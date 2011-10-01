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
*/
package org.fabric3.federation.provisioning;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServlet;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.host.ServletHost;

/**
 * Processes a request for the provisioning URL of a contribution artifact. This implementation uses a Servlet provided by subclasses to provision
 * artifacts via HTTP.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public abstract class AbstractProvisionCommandExecutor implements CommandExecutor<ProvisionCommand> {
    private ServletHost host;
    private CommandExecutorRegistry registry;
    private boolean secure;
    private String address;
    private String mappingPath = "repository";
    protected ProvisionMonitor monitor;
    protected String role = "provision.client";


    public AbstractProvisionCommandExecutor(ServletHost host, CommandExecutorRegistry registry, ProvisionMonitor monitor) {
        this.host = host;
        this.registry = registry;
        this.monitor = monitor;
    }

    /**
     * Optional property to set if provisioning should only be done using HTTP and authentication.
     *
     * @param secure true if provisioning should only be done using HTTP and authentication
     */
    @Property(required = false)
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Role required by subjects authenticating to provision a contribution.
     *
     * @param role role required by subjects authenticating to provision a contribution
     */
    @Property(required = false)
    public void setRole(String role) {
        this.role = role;
    }

    @Property(required = false)
    public void setMappingPath(String path) {
        mappingPath = path;
    }

    @Property(required = false)
    public void setAddress(String address) {
        this.address = address;
    }

    @Init
    public void init() throws UnknownHostException {
        if (address == null) {
            address = InetAddress.getLocalHost().getHostAddress();
        }
        HttpServlet servlet;
        if (secure && !host.isHttpsEnabled()) {
            monitor.httpsNotEnabled();
            servlet = getResolverServlet(false);
        } else {
            servlet = getResolverServlet(secure);
        }
        host.registerMapping("/" + mappingPath + "/*", servlet);
        registry.register(ProvisionCommand.class, this);
    }

    public void execute(ProvisionCommand command) throws ExecutionException {
        try {
            URI contributionUri = command.getContributionUri();
            String path = "/" + mappingPath;
            URL contributionUrl;
            if (secure) {
                contributionUrl = new URL("https://" + address + ":" + host.getHttpsPort() + path + "/" + contributionUri);
            } else {
                contributionUrl = new URL("http://" + address + ":" + host.getHttpPort() + path + "/" + contributionUri);
            }
            ProvisionResponse response = new ProvisionResponse(contributionUrl);
            command.setResponse(response);
        } catch (MalformedURLException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Returns a servlet that is responsible for de-referencing a contribution artifact from a store.
     *
     * @param secure true if the servlet should enforce secure provisioning
     * @return a servlet that is responsible for de-referencing a contribution artifact from a store
     */
    protected abstract HttpServlet getResolverServlet(boolean secure);

}