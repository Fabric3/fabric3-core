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

import java.util.Map;
import java.util.Properties;

import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import static com.sun.enterprise.ee.cms.core.GroupManagementService.MemberType.CORE;
import com.sun.enterprise.ee.cms.impl.client.FailureNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.JoinNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.JoinedAndReadyNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.PlannedShutdownActionFactoryImpl;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.Names;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.topology.RuntimeService;

/**
 * Manages federated communications using Shoal on a controller instance.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces = {RuntimeService.class, FederationService.class})
public class ControllerFederationService extends AbstractFederationService implements RuntimeService, FederationService {
    private ClassLoaderRegistry classLoaderRegistry;

    /**
     * Constructor
     *
     * @param eventService        the runtime event service
     * @param classLoaderRegistry the classloader registry
     * @param info                the host runtime information
     * @param monitor             the monitor for controller events
     */
    public ControllerFederationService(@Reference EventService eventService,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference HostInfo info,
                                       @Monitor FederationServiceMonitor monitor) {
        super(eventService, info, monitor);
        this.classLoaderRegistry = classLoaderRegistry;
    }

    protected synchronized void onStartCommunications(Properties properties) {
        try {
            domainGMS = (GroupManagementService) GMSFactory.startGMSModule(runtimeName, domainName, CORE, properties);
            monitor.joined(domainName, runtimeName);
            initializeCallbacks(domainGMS);
            domainGMS.join();
            domainGMS.reportJoinedAndReadyState(domainName);
            for (FederationCallback callback : callbacks.values()) {
                callback.afterJoin();
            }
        } catch (GMSException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    /**
     * Callback when the runtime shuts down.
     */
    protected synchronized void onStopCommunications() {
        try {
            if (domainGMS != null) {
                domainGMS.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                for (FederationCallback callback : callbacks.values()) {
                    callback.onLeave();
                }
                monitor.exited(domainName);
            }
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }


    private void initializeCallbacks(GroupManagementService gms) {
        SignalBroadcaster broadcaster = new SignalBroadcaster(callbacks.values(), monitor);
        gms.addActionFactory(new PlannedShutdownActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new FailureNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinedAndReadyNotificationActionFactoryImpl(broadcaster));
        for (Map.Entry<String, FederationCallback> entry : callbacks.entrySet()) {
            String serviceName = entry.getKey();
            // TODO handle failure recovery
            // DispatchingFailureRecoveryActionFactory factory =new DispatchingFailureRecoveryActionFactory(serviceName, entry.getValue(), monitor)
            // gms.addActionFactory(serviceName, factory, serviceName));
            ClassLoader hostClassLoader = classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION);
            DispatchingMessageActionFactory factory = new DispatchingMessageActionFactory(serviceName, entry.getValue(), hostClassLoader, monitor);
            gms.addActionFactory(factory, serviceName);
        }
    }

}