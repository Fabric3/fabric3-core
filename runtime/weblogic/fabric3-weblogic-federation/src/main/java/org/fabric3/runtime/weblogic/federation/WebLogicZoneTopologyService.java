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
package org.fabric3.runtime.weblogic.federation;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.management.JMException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.federation.deployment.command.DeploymentCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateCommand;
import org.fabric3.federation.deployment.command.RuntimeUpdateResponse;
import org.fabric3.runtime.weblogic.cluster.ChannelException;
import org.fabric3.runtime.weblogic.cluster.RuntimeChannel;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.Response;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;

import static org.fabric3.runtime.weblogic.federation.Constants.CONTROLLER_CONTEXT;
import static org.fabric3.runtime.weblogic.federation.Constants.PARTICIPANT_CONTEXT;

/**
 * Provides domain communication for a participant runtime using the WebLogic clustered JNDI tree.
 *
 * @version $Rev$ $Date$
 */
@Service(ZoneTopologyService.class)
@EagerInit
public class WebLogicZoneTopologyService implements ZoneTopologyService {
    private static final String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

    private ExecutorService executorService;
    private WebLogicTopologyMonitor monitor;
    private EventService eventService;
    private SerializationService serializationService;
    private CommandExecutorRegistry executorRegistry;
    private JmxHelper jmxHelper;
    private RuntimeChannel runtimeChannel;

    private RuntimeChannel controllerChannel;
    private String runtimeName;
    private String adminServerUrl = "t3://localhost:7001";
    private boolean synchronize = true;
    private String zoneName;

    public WebLogicZoneTopologyService(@Reference EventService eventService,
                                       @Reference SerializationService serializationService,
                                       @Reference CommandExecutorRegistry executorRegistry,
                                       @Reference ExecutorService executorService,
                                       @Reference JmxHelper jmxHelper,
                                       @Monitor WebLogicTopologyMonitor monitor) {
        this.eventService = eventService;
        this.serializationService = serializationService;
        this.executorRegistry = executorRegistry;
        this.jmxHelper = jmxHelper;
        this.executorService = executorService;
        this.monitor = monitor;
    }

    @Property(required = false)
    public void setAdminServerUrl(String adminServerUrl) {
        this.adminServerUrl = adminServerUrl;
    }

    /**
     * Property to configure whether the runtime should attempt an update by controller.
     *
     * @param synchronize true if the runtime should attempt an update (the default)
     */
    @Property(required = false)
    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }

    @Init
    public void init() throws JMException {
        runtimeName = jmxHelper.getRuntimeJmxAttribute(String.class, "ServerRuntime/Name");
        zoneName = jmxHelper.getRuntimeJmxAttribute(String.class, "DomainConfiguration/Name");
        eventService.subscribe(JoinDomain.class, new JoinDomainListener());
        runtimeChannel = new RuntimeChannelImpl(runtimeName, executorRegistry, serializationService, monitor);
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public boolean isZoneLeader() {
        return false;
    }

    public boolean supportsDynamicChannels() {
        return false;
    }

    public void register(TopologyListener listener) {

    }

    public void deregister(TopologyListener listener) {

    }

    public void registerMetadata(String key, Serializable metadata) {

    }

    public boolean isControllerAvailable() {
        return true;
    }

    public String getZoneLeaderName() {
        return null;
    }

    public Response sendSynchronousToController(ResponseCommand command, long timeout) throws MessageException {
        try {
            byte[] payload = serializationService.serialize(command);
            byte[] responsePayload = controllerChannel.sendSynchronous(payload);
            return serializationService.deserialize(Response.class, responsePayload);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        } catch (ClassNotFoundException e) {
            throw new MessageException(e);
        }
    }

    public void broadcast(Command command) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public void sendAsynchronous(String runtimeName, Command command) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public Response sendSynchronous(String runtimeName, ResponseCommand command, long timeout) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public List<Response> sendSynchronous(ResponseCommand command, long timeout) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public void sendAsynchronousToController(Command command) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public void openChannel(String name, String configuration, MessageReceiver receiver) throws ZoneChannelException {
        throw new UnsupportedOperationException();
    }

    public void closeChannel(String name) throws ZoneChannelException {
        throw new UnsupportedOperationException();
    }

    public void sendAsynchronous(String name, Serializable message) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public void sendAsynchronous(String runtimeName, String name, Serializable message) throws MessageException {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes JNDI contexts used for domain communications.
     *
     * @return true if the contexts were initialized; false if there was an error
     */
    private boolean initJndiContexts() {
        monitor.connectingToAdminServer();
        Context rootContext;
        try {
            // lookup the controller context on the admin server
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, adminServerUrl);
            rootContext = new InitialContext(env);
        } catch (NamingException e) {
            monitor.errorMessage("Error connecting to admin server", e);
            return false;
        }
        Context controllerContext = null;
        Context participantContext = null;
        try {
            controllerContext = JndiHelper.getContext(CONTROLLER_CONTEXT, rootContext);
            try {
                // lookup the controller channel RMI stub
                controllerChannel = (RuntimeChannel) controllerContext.lookup(Constants.CONTROLLER_CHANNEL);
            } catch (NameNotFoundException e) {
                monitor.errorMessage("Unable to contact admin server", e);
            }
            participantContext = JndiHelper.getContext(PARTICIPANT_CONTEXT, rootContext);
            try {
                participantContext.bind(runtimeName, runtimeChannel);
            } catch (NameAlreadyBoundException e) {
                participantContext.rebind(runtimeName, runtimeChannel);
            }
            return true;
        } catch (NamingException e) {
            monitor.errorMessage("Error joining the domain", e);
            return false;
        } finally {
            JndiHelper.close(rootContext, participantContext, controllerContext);
        }
    }

    /**
     * Performs a runtime update by querying the admin server.
     *
     * @return true if the runtime was updated
     */
    private boolean update() {
        if (!synchronize) {
            return true;
        }
        monitor.updating();
        RuntimeUpdateCommand command = new RuntimeUpdateCommand(runtimeName, zoneName, null);
        Response response;
        try {
            byte[] payload = serializationService.serialize(command);
            byte[] responsePayload = controllerChannel.sendSynchronous(payload);
            response = serializationService.deserialize(Response.class, responsePayload);
        } catch (RemoteException e) {
            monitor.error(e);
            return false;
        } catch (ChannelException e) {
            monitor.error(e);
            return false;
        } catch (IOException e) {
            // programming error
            monitor.error(e);
            return false;
        } catch (ClassNotFoundException e) {
            // programming error
            monitor.error(e);
            return false;
        }
        assert response instanceof RuntimeUpdateResponse;
        RuntimeUpdateResponse updateResponse = (RuntimeUpdateResponse) response;
        if (!updateResponse.isUpdated()) {
            // not updated, wait until a controller becomes available
            return false;
        }
        try {
            DeploymentCommand deploymentCommand = updateResponse.getDeploymentCommand();
            executorRegistry.execute(deploymentCommand);
        } catch (ExecutionException e) {
            monitor.error(e);
            // return true to avoid multiple attempts to update the runtime in the case of a deployment error
            return true;
        }
        monitor.updated();
        return true;
    }


    /**
     * Event listener that binds the runtime Channel to the JNDI tree when the JoinDomain event is fired.
     */
    private class JoinDomainListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            boolean result = initJndiContexts();
            if (!result) {
                monitor.adminServerUnavailable();
                // admin server is not available, schedule work to retry periodically
                executorService.execute(new Work());
                return;
            }
            update();
        }
    }

    /**
     * Used to asynchronously attempt to initialize and update the runtime if the controller is not available when it boots. This polling mechanism is
     * required as WebLogic remote JNDI contexts do not implement EventContext to receive callbacks when a JNDI object changes (such as a controller
     * channel becoming available).
     */
    private class Work implements Runnable {

        public void run() {
            while (true) {
                if (initJndiContexts() && update()) {
                    return;
                }
                try {
                    Thread.sleep(30000);  // wait 30 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
        }
    }

}
