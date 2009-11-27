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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.enterprise.ee.cms.core.FailureNotificationSignal;
import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.JoinNotificationSignal;
import com.sun.enterprise.ee.cms.core.MessageSignal;
import com.sun.enterprise.ee.cms.core.PlannedShutdownSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import static org.fabric3.federation.shoal.FederationConstants.DOMAIN_MANAGER;
import static org.fabric3.federation.shoal.FederationConstants.ZONE_MANAGER;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.topology.DomainManager;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.Zone;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class ShoalDomainManager implements DomainManager, FederationCallback {
    private FederationService federationService;
    private CommandExecutorRegistry executorRegistry;
    private DomainManagerMonitor monitor;
    private ClassLoaderRegistry classLoaderRegistry;

    /**
     * Constructor
     *
     * @param federationService   the service responsible for managing domain runtime communications
     * @param executorRegistry    the command executor registry
     * @param classLoaderRegistry the classloader registry
     * @param monitor             the monitor for reporting events
     */
    public ShoalDomainManager(@Reference FederationService federationService,
                              @Reference CommandExecutorRegistry executorRegistry,
                              @Reference ClassLoaderRegistry classLoaderRegistry,
                              @Monitor DomainManagerMonitor monitor) {
        this.federationService = federationService;
        this.executorRegistry = executorRegistry;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        federationService.registerCallback(DOMAIN_MANAGER, this);
    }

    public List<Zone> getZones() {
        List<String> members = federationService.getDomainGMS().getGroupHandle().getCurrentCoreMembers();
        List<Zone> zones = new ArrayList<Zone>(members.size());
        for (String member : members) {
            // FIXME we need a way to distinguish member types, possibly by using member attributes
            // Don't include controller.
            if (!member.equals(federationService.getRuntimeName())) {
                zones.add(new Zone(member));
            }
        }
        return zones;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getTransportMetaData(String zone, Class<T> type, String transport) {
        Map<Serializable, Serializable> details = federationService.getDomainGMS().getMemberDetails(zone);
        if (details == null) {
            return null;
        }
        Map<String, T> transportMetadata = (Map<String, T>) details.get(FederationConstants.ZONE_TRANSPORT_INFO);
        if (transportMetadata == null) {
            throw new AssertionError("Transport metadata not found");
        }
        return transportMetadata.get(transport);
    }

    public void sendMessage(String zoneName, byte[] message) throws MessageException {
        try {
            federationService.getDomainGMS().getGroupHandle().sendMessage(zoneName, ZONE_MANAGER, message);
        } catch (GMSException e) {
            throw new MessageException(e);
        }
    }


    public void afterJoin() {
        // no op
    }

    public void onLeave() {
        // no op
    }

    public void onSignal(Signal signal) throws FederationCallbackException {
        if (signal instanceof MessageSignal) {
            executeCommand((MessageSignal) signal);
        } else if (signal instanceof JoinNotificationSignal) {
            monitor.joined(signal.getMemberToken());
        } else if (signal instanceof FailureNotificationSignal) {
            monitor.failed(signal.getMemberToken());
        } else if (signal instanceof PlannedShutdownSignal) {
            monitor.shutdown(signal.getMemberToken());
        }
    }

    @SuppressWarnings({"unchecked"})
    private void executeCommand(MessageSignal signal) throws FederationCallbackException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            byte[] payload = signal.getMessage();
            InputStream stream = new ByteArrayInputStream(payload);
            // Deserialize the command. As command classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            Command command = (Command) ois.readObject();
            executorRegistry.execute(command);
        } catch (ExecutionException e) {
            throw new FederationCallbackException(e);
        } catch (IOException e) {
            throw new FederationCallbackException(e);
        } catch (ClassNotFoundException e) {
            throw new FederationCallbackException(e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                // ignore;
            }
        }
    }

}

