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
package org.fabric3.runtime.weblogic.federation;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.management.JMException;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

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
import static org.fabric3.runtime.weblogic.federation.Constants.DYNAMIC_CHANNEL_CONTEXT;
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

    private List<ChannelOpenRequest> channelRequests = new ArrayList<ChannelOpenRequest>();

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

    public Response sendSynchronous(String destinationName, ResponseCommand command, long timeout) throws MessageException {
        Context rootContext = null;
        try {
            rootContext = getRootContext();
            byte[] payload = serializationService.serialize(command);
            NamingEnumeration<Binding> enumeration = rootContext.listBindings(PARTICIPANT_CONTEXT);
            while (enumeration.hasMoreElements()) {
                Binding binding = enumeration.next();
                if (RuntimeChannel.class.getName().equals(binding.getClassName())) {
                    RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                    if (destinationName.equals(channel.getRuntimeName())) {
                        byte[] responsePayload = runtimeChannel.sendSynchronous(payload);
                        return serializationService.deserialize(Response.class, responsePayload);
                    }
                }
            }
            throw new MessageException("Runtime not found: " + destinationName);
        } catch (NamingException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        } catch (ClassNotFoundException e) {
            throw new MessageException(e);
        } finally {
            JndiHelper.close(rootContext);
        }
    }

    public List<Response> sendSynchronous(ResponseCommand command, long timeout) throws MessageException {
        List<Response> responses = new ArrayList<Response>();
        Context rootContext = null;
        try {
            rootContext = getRootContext();
            byte[] payload = serializationService.serialize(command);
            NamingEnumeration<Binding> enumeration = rootContext.listBindings(PARTICIPANT_CONTEXT);
            if (!enumeration.hasMoreElements()) {
                throw new MessageException("No runtimes in domain");
            }
            while (enumeration.hasMoreElements()) {
                Binding binding = enumeration.next();
                if (RuntimeChannel.class.getName().equals(binding.getClassName())) {
                    RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                    if (runtimeName.equals(channel.getRuntimeName())) {
                        // don't send to self
                        continue;
                    }
                    byte[] responsePayload = channel.sendSynchronous(payload);
                    Response response = serializationService.deserialize(Response.class, responsePayload);
                    responses.add(response);
                }
            }
            return responses;
        } catch (NamingException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        } catch (ClassNotFoundException e) {
            throw new MessageException(e);
        } finally {
            JndiHelper.close(rootContext);
        }
    }

    public void sendAsynchronousToController(Command command) throws MessageException {
        throw new UnsupportedOperationException();
    }

    public void openChannel(String name, String configuration, MessageReceiver receiver) throws ZoneChannelException {
        Context rootContext = null;
        Context dynamicChannelContext = null;
        RuntimeChannelImpl channel = new RuntimeChannelImpl(runtimeName, executorRegistry, serializationService, receiver, monitor);
        try {
            rootContext = getRootContext();
            dynamicChannelContext = JndiHelper.getContext(DYNAMIC_CHANNEL_CONTEXT, rootContext);
            dynamicChannelContext.bind(name + ":" + runtimeName, channel);
        } catch (NameAlreadyBoundException e) {
            try {
                dynamicChannelContext.rebind(name + ":" + runtimeName, channel);
            } catch (NamingException ex) {
                // controller may not be available
                monitor.errorMessage("Controller not available - queueing request for retry", ex);
                channelRequests.add(new ChannelOpenRequest(name, receiver));
            }
        } catch (NamingException e) {
            // controller may not be available
            monitor.errorMessage("Controller not available - queueing request for retry", e);
            channelRequests.add(new ChannelOpenRequest(name, receiver));
        } finally {
            JndiHelper.close(rootContext, dynamicChannelContext);
        }
    }

    public void closeChannel(String name) throws ZoneChannelException {
        Context rootContext = null;
        Context dynamicChannelContext = null;
        try {
            rootContext = getRootContext();
            dynamicChannelContext = JndiHelper.getContext(DYNAMIC_CHANNEL_CONTEXT, rootContext);
            dynamicChannelContext.unbind(name + ":" + runtimeName);
        } catch (NamingException e) {
            throw new ZoneChannelException(e);
        } finally {
            JndiHelper.close(rootContext, dynamicChannelContext);
        }
    }

    public void sendAsynchronous(String name, Serializable message) throws MessageException {
        Context rootContext = null;
        try {
            rootContext = getRootContext();
            byte[] payload = serializationService.serialize(message);
            NamingEnumeration<Binding> enumeration = rootContext.listBindings(DYNAMIC_CHANNEL_CONTEXT);
            while (enumeration.hasMoreElements()) {
                Binding binding = enumeration.next();
                if (RuntimeChannel.class.getName().equals(binding.getClassName())) {
                    RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                    if (binding.getName().startsWith(name + ":")) {
                        if (channel.getRuntimeName().equals(runtimeName)) {
                            // don't send to self
                            continue;
                        }
                        channel.publish(payload);
                    }
                }
            }
        } catch (NamingException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        } finally {
            JndiHelper.close(rootContext);
        }
    }

    public void sendAsynchronous(String destinationName, String name, Serializable message) throws MessageException {
        Context rootContext = null;
        try {
            rootContext = getRootContext();
            byte[] payload = serializationService.serialize(message);
            NamingEnumeration<Binding> enumeration = rootContext.listBindings(DYNAMIC_CHANNEL_CONTEXT);
            while (enumeration.hasMoreElements()) {
                Binding binding = enumeration.next();
                if (RuntimeChannel.class.getName().equals(binding.getClassName())) {
                    RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                    if (binding.getName().equals(name + ":" + destinationName)) {
                        channel.publish(payload);
                        return;
                    }
                }
            }
            throw new MessageException("Runtime not found: " + destinationName);
        } catch (NamingException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        } finally {
            JndiHelper.close(rootContext);
        }
    }

    /**
     * Initializes JNDI contexts used for domain communications.
     *
     * @return true if the contexts were initialized; false if there was an error
     */
    private boolean initJndiContexts() {
        monitor.connectingToAdminServer();
        Context rootContext;
        Context participantContext = null;
        Context controllerContext = null;
        Context dynamicChannelContext = null;
        try {
            rootContext = getRootContext();
        } catch (NamingException e) {
            monitor.errorMessage("Error connecting to admin server", e);
            return false;
        }
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

            dynamicChannelContext = JndiHelper.getContext(DYNAMIC_CHANNEL_CONTEXT, rootContext);

            // initialize dynamic channels
            for (ChannelOpenRequest request : channelRequests) {
                MessageReceiver receiver = request.getReceiver();
                RuntimeChannelImpl channel = new RuntimeChannelImpl(runtimeName, executorRegistry, serializationService, receiver, monitor);
                try {
                    dynamicChannelContext.bind(request.getName() + ":" + runtimeName, channel);
                } catch (NameAlreadyBoundException e) {
                    dynamicChannelContext.rebind(request.getName() + ":" + runtimeName, channel);
                }
            }

            return true;
        } catch (NamingException e) {
            monitor.errorMessage("Error joining the domain", e);
            return false;
        } finally {
            JndiHelper.close(participantContext, controllerContext, rootContext, dynamicChannelContext);
        }
    }

    private Context getRootContext() throws NamingException {
        Context rootContext;// lookup the controller context on the admin server
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        env.put(Context.PROVIDER_URL, adminServerUrl);
        rootContext = new InitialContext(env);
        return rootContext;
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

    private class ChannelOpenRequest {
        private String name;
        private MessageReceiver receiver;

        private ChannelOpenRequest(String name, MessageReceiver receiver) {
            this.name = name;
            this.receiver = receiver;
        }

        public String getName() {
            return name;
        }

        public MessageReceiver getReceiver() {
            return receiver;
        }
    }

}
