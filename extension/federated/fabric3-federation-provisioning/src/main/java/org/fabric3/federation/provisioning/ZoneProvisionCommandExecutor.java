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

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.repository.ArtifactCache;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthorizationService;

/**
 * Used on a participant to processes a request for the provisioning URL of a contribution artifact
 */
@EagerInit
public class ZoneProvisionCommandExecutor extends AbstractProvisionCommandExecutor {
    private AuthenticationService authenticationService;
    private AuthorizationService authorizationService;
    private ArtifactCache cache;

    public ZoneProvisionCommandExecutor(@Reference ServletHost host,
                                        @Reference AuthenticationService authenticationService,
                                        @Reference AuthorizationService authorizationService,
                                        @Reference CommandExecutorRegistry registry,
                                        @Reference ArtifactCache cache,
                                        @Monitor ProvisionMonitor monitor) {
        super(host, registry, monitor);
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.cache = cache;
    }

    protected HttpServlet getResolverServlet(boolean secure) {
        if (secure) {
            return new ArtifactCacheResolverServlet(cache, authenticationService, authorizationService, role, monitor);
        } else {
            return new ArtifactCacheResolverServlet(cache, monitor);
        }
    }
}