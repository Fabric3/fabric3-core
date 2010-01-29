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
*/
package org.fabric3.federation.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.ContributionResolverExtension;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.Response;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * Resolves contributions in a domain. Resolution is done by first querying a zone leader for the contribution URL. If the current runtime is the zone
 * leader, the controller is queried for the contribution URL.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ZoneContributionResolverExtension implements ContributionResolverExtension {
    private ZoneTopologyService topologyService;
    private long defaultTimeout = 10000;

    public ZoneContributionResolverExtension(@Reference ZoneTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Property(required = false)
    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public InputStream resolve(URI contributionUri) throws ResolutionException {
        String zoneLeader = topologyService.getZoneLeader();
        ProvisionCommand command = new ProvisionCommand(contributionUri);
        try {
            Response val;
            if (!topologyService.isZoneLeader() && zoneLeader != null) {
                // query the zone leader
                val = topologyService.sendSynchronous(zoneLeader, command, defaultTimeout);
            } else {
                // query the controller
                val = topologyService.sendSynchronousToController(command, defaultTimeout);
            }
            ProvisionResponse response = (ProvisionResponse) val;
            return response.getContributionUrl().openStream();
        } catch (MessageException e) {
            throw new ResolutionException(e);
        } catch (IOException e) {
            throw new ResolutionException(e);
        }
    }

}