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
package org.fabric3.federation.provisioning;

import javax.servlet.http.HttpServlet;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.executor.CommandExecutor;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.container.executor.ExecutionException;
import org.fabric3.spi.host.ServletHost;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;

/**
 * Processes a request for the provisioning URL of a contribution artifact. This implementation uses a Servlet provided by subclasses to provision
 * artifacts via HTTP.
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

    public void execute(ProvisionCommand command) throws ContainerException {
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