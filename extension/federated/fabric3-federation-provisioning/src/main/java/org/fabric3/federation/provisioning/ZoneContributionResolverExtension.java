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
package org.fabric3.federation.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.federation.topology.MessageException;

/**
 * Resolves contributions in a domain. Resolution is done by first querying a the controller for the contribution URL. If the controller is
 * unavailable, the zone leader is queried.
 */
@EagerInit
public class ZoneContributionResolverExtension implements ContributionResolverExtension {
    private ParticipantTopologyService topologyService;
    private ProvisionMonitor monitor;
    private boolean secure;
    private String username;
    private String password;
    private long defaultTimeout = 10000;

    public ZoneContributionResolverExtension(@Reference ParticipantTopologyService topologyService, @Monitor ProvisionMonitor monitor) {
        this.topologyService = topologyService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Property(required = false)
    public void setUsername(String username) {
        this.username = username;
    }

    @Property(required = false)
    public void setPassword(String password) {
        this.password = password;
    }

    @Property(required = false)
    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    @Init
    public void init() {
        if (secure) {
            if (username == null) {
                monitor.warnUsername();
            }
            if (password == null) {
                monitor.warnPassword();
            }
        }
    }

    public InputStream resolve(URI contributionUri) throws ResolutionException {
        String zoneLeader = topologyService.getZoneLeaderName();
        URL resolveURL = null;
        ProvisionCommand command = new ProvisionCommand(contributionUri);
        try {
            ProvisionResponse response;
            if (topologyService.isControllerAvailable()) {
                // query the controller
                response = (ProvisionResponse) topologyService.sendSynchronousToController(command, defaultTimeout);
            } else if (!topologyService.isZoneLeader() && zoneLeader != null) {
                // query the zone leader
                response = (ProvisionResponse) topologyService.sendSynchronous(zoneLeader, command, defaultTimeout);
            } else {
                throw new ResolutionException("Unable to contact controller or peer to resolve contribution: " + contributionUri);
            }
            resolveURL = response.getContributionUrl();
            if (secure) {
                resolveURL = new URL(resolveURL.toString() + "?username=" + username + "&password=" + password);
            }
            monitor.resolving(resolveURL);
            return resolveURL.openStream();
        } catch (MessageException e) {
            monitor.error("Error while sending provisioning command", e);
            throw new ResolutionException(e);
        } catch (IOException e) {
            monitor.error("Cannot resolve contribution from URL: " + resolveURL, e);
            throw new ResolutionException(e);
        }
    }

}