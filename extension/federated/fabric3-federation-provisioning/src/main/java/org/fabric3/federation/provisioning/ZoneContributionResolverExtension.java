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
package org.fabric3.federation.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * Resolves contributions in a domain. Resolution is done by first querying a the controller for the contribution URL. If the controller is
 * unavailable, the zone leader is queried.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ZoneContributionResolverExtension implements ContributionResolverExtension {
    private ZoneTopologyService topologyService;
    private ProvisionMonitor monitor;
    private boolean secure;
    private String username;
    private String password;
    private long defaultTimeout = 10000;

    public ZoneContributionResolverExtension(@Reference ZoneTopologyService topologyService, @Monitor ProvisionMonitor monitor) {
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