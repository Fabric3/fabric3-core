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
package org.fabric3.federation.shoal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.enterprise.ee.cms.core.GMSException;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.PaticipantSyncCommand;
import org.fabric3.federation.command.ZoneSyncCommand;
import org.fabric3.federation.event.RuntimeSynchronized;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3Event;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.topology.ZoneManager;

/**
 * Responsible for synchronizing a participant with the domain. If the node is a participant, a synchronization request will be sent to the
 * participant that is the zone manager. If the node is elected as a zone manager, the synchronization will be sent directly to the domain
 * controller.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DomainSynchronizer implements Runnable, Fabric3EventListener {
    private ParticipantFederationService federationService;
    private ZoneManager zoneManager;
    private EventService eventService;
    private DomainSynchronizerMonitor monitor;
    private ScheduledExecutorService executor;

    public DomainSynchronizer(@Reference ParticipantFederationService federationService,
                              @Reference ZoneManager zoneManager,
                              @Reference EventService eventService,
                              @Monitor DomainSynchronizerMonitor monitor) {
        this.federationService = federationService;
        this.zoneManager = zoneManager;
        this.eventService = eventService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeSynchronized.class, this);
        eventService.subscribe(RuntimeStart.class, this);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
    }

    public void run() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        MultiClassLoaderObjectOutputStream stream;
        try {
            stream = new MultiClassLoaderObjectOutputStream(bas);
            String name = federationService.getRuntimeName();
            String zoneName = federationService.getZoneName();
            if (zoneManager.isZoneManager()) {
                monitor.synchronizingWithController();
                ZoneSyncCommand command = new ZoneSyncCommand(zoneName, name);
                stream.writeObject(command);
                stream.close();
                // XCV FIXME avoid sending to all runtimes in the zone
                federationService.getDomainGMS().getGroupHandle().sendMessage(FederationConstants.DOMAIN_MANAGER, bas.toByteArray());
            } else {
                monitor.synchronizingWithZoneManager();
                PaticipantSyncCommand command = new PaticipantSyncCommand(name);
                stream.writeObject(command);
                stream.close();
                String leader = federationService.getZoneGMS().getGroupHandle().getGroupLeader();
                federationService.getZoneGMS().getGroupHandle().sendMessage(leader, FederationConstants.ZONE_MANAGER, bas.toByteArray());
            }
        } catch (IOException e) {
            monitor.error(e);
        } catch (GMSException e) {
            monitor.error(e);
        }
    }

    public void onEvent(Fabric3Event event) {
        if (event instanceof RuntimeSynchronized) {
            if (executor.isShutdown()) {
                return;
            }
            executor.shutdownNow();
        } else if (event instanceof RuntimeStart) {
            executor.scheduleWithFixedDelay(this, 3000, 3000, TimeUnit.MILLISECONDS);
        }
    }
}
