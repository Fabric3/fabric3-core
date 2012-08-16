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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import org.fabric3.api.annotation.monitor.Monitor;
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
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.RuntimeInstance;
import org.fabric3.spi.federation.Zone;

import static org.fabric3.runtime.weblogic.api.Constants.WLS_RUNTIME_SERVICE_MBEAN;
import static org.fabric3.runtime.weblogic.federation.Constants.CONTROLLER_CHANNEL;
import static org.fabric3.runtime.weblogic.federation.Constants.PARTICIPANT_CONTEXT;

/**
 * Provides domain-wide controller communication using the WebLogic clustered JNDI tree.
 *
 * @version $Rev$ $Date$
 */
@Service(DomainTopologyService.class)
@EagerInit
public class WebLogicDomainTopologyService implements DomainTopologyService {
    private static final String FABRIC3_WEBLOGIC_HOST = "fabric3-weblogic-host";

    // The WLS application activated state. Other WLS states:  UNPREPARED = 0; PREPARED = 1; NEW = 3; UPDATE_PENDING = 4
    private static final int WLS_ACTIVATED_STATE = 2;

    private String runtimeName = "controller";
    private CommandExecutorRegistry executorRegistry;
    private EventService eventService;
    private SerializationService serializationService;
    private MBeanServer mBeanServer;
    private WebLogicTopologyMonitor monitor;
    private InitialContext rootContext;
    private EventContext participantContext;

    public WebLogicDomainTopologyService(@Reference CommandExecutorRegistry executorRegistry,
                                         @Reference EventService eventService,
                                         @Reference SerializationService serializationService,
                                         @Reference MBeanServer mBeanServer,
                                         @Monitor WebLogicTopologyMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.eventService = eventService;
        this.serializationService = serializationService;
        this.mBeanServer = mBeanServer;
        this.monitor = monitor;
    }

    @Init
    public void init() throws NamingException {
        eventService.subscribe(JoinDomain.class, new JoinDomainListener());
    }

    @Destroy
    public void destroy() throws NamingException {
        if (rootContext != null) {
            rootContext.unbind(CONTROLLER_CHANNEL);
            rootContext.close();
        }
        if (participantContext != null) {
            participantContext.close();
        }
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public Set<Zone> getZones() {
        return null;
    }

    public List<RuntimeInstance> getRuntimes() {
        List<RuntimeInstance> instances = new ArrayList<RuntimeInstance>();
        try {
            NamingEnumeration<Binding> list = participantContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                String runtimeName = channel.getRuntimeName();
                Map<String, Serializable> map = Collections.emptyMap();
                RuntimeInstance runtimeInstance = new RuntimeInstance(runtimeName, map);
                instances.add(runtimeInstance);
            }
        } catch (NamingException e) {
            monitor.error(e);
        } catch (RemoteException e) {
            monitor.error(e);
        }
        return instances;
    }

    public String getTransportMetaData(String zone, String transport) {
        return null;
    }

    public void broadcast(Command command) throws MessageException {
        try {
            byte[] payload = serializationService.serialize(command);
            NamingEnumeration<Binding> list = rootContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                channel.send(payload);
            }
        } catch (NamingException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        }
    }

    public void broadcast(String zoneName, Command command) throws MessageException {
        List<RuntimeChannel> channels = getChannels();
        try {
            byte[] payload = serializationService.serialize(command);
            for (RuntimeChannel channel : channels) {
                channel.send(payload);
            }
        } catch (IOException e) {
            throw new MessageException(e);
        } catch (ChannelException e) {
            throw new MessageException(e);
        }
    }

    public List<Response> sendSynchronousToZone(String zoneName, ResponseCommand command, boolean failFast, long timeout) throws MessageException {
        List<RuntimeChannel> channels = getChannels();
        if (channels.isEmpty()) {
            throw new MessageException("No managed servers found to deploy to");
        }
        List<Response> responses = new ArrayList<Response>();
        byte[] payload;
        try {
            payload = serializationService.serialize(command);
        } catch (IOException e) {
            throw new MessageException(e);
        }
        for (RuntimeChannel channel : channels) {
            try {
                byte[] responsePayload = channel.sendSynchronous(payload);
                Response response = serializationService.deserialize(Response.class, responsePayload);
                responses.add(response);
                // TODO handle exceptions and rollback
            } catch (ChannelException e) {
                throw new MessageException(e);
            } catch (RemoteException e) {
                throw new MessageException(e);
            } catch (ClassNotFoundException e) {
                throw new MessageException(e);
            } catch (IOException e) {
                throw new MessageException(e);
            }
        }
        return responses;
    }

    public Response sendSynchronous(String runtimeName, ResponseCommand command, long timeout) throws MessageException {
        try {
            RuntimeChannel runtimeChannel = null;
            NamingEnumeration<Binding> list = participantContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                if (runtimeName.equals(channel.getRuntimeName())) {
                    runtimeChannel = channel;
                    break;
                }
            }
            if (runtimeChannel == null) {
                // TODO throw specific exception type
                throw new MessageException("Runtime not found: " + runtimeName);
            }
            byte[] payload = serializationService.serialize(command);
            byte[] responsePayload = runtimeChannel.sendSynchronous(payload);
            return serializationService.deserialize(Response.class, responsePayload);
        } catch (ChannelException e) {
            throw new MessageException(e);
        } catch (NamingException e) {
            throw new MessageException(e);
        } catch (RemoteException e) {
            throw new MessageException(e);
        } catch (IOException e) {
            throw new MessageException(e);
        } catch (ClassNotFoundException e) {
            throw new MessageException(e);
        }
    }

    private List<RuntimeChannel> getChannels() throws MessageException {
        try {
            List<RuntimeChannel> channels = new ArrayList<RuntimeChannel>();
            NamingEnumeration<Binding> list = rootContext.listBindings(PARTICIPANT_CONTEXT);
            while (list.hasMore()) {
                Binding binding = list.next();
                RuntimeChannel channel = (RuntimeChannel) binding.getObject();
                channels.add(channel);
            }
            return channels;
        } catch (NameNotFoundException e) {
            // managed servers may not have initialized
            monitor.noManagedServers();
            return Collections.emptyList();
        } catch (NamingException e) {
            throw new MessageException(e);
        }
    }

    private void bindController() {
        try {
            RuntimeChannel controllerChannel = new RuntimeChannelImpl(runtimeName, executorRegistry, serializationService, monitor);
            rootContext = new InitialContext();
            Context controllerContext = JndiHelper.getContext(Constants.CONTROLLER_CONTEXT, rootContext);
            try {
                controllerContext.bind(CONTROLLER_CHANNEL, controllerChannel);
            } catch (NameAlreadyBoundException e) {
                controllerContext.rebind(CONTROLLER_CHANNEL, controllerChannel);
            }
            Context ctx = JndiHelper.getContext(PARTICIPANT_CONTEXT, rootContext);
            assert ctx instanceof EventContext;
            participantContext = (EventContext) ctx;
        } catch (NamingException e) {
            monitor.errorMessage("Error initializing domain topology service", e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Joins the domain by binding controller federation channels into the JNDI tree.
     * <p/>
     * Note that joining may happen asynchronously if the F3 host application has not been activated. This is to avoid the race condition where an
     * application is deployed and a participant attempts to provision a contribution over HTTP before the provisioning servlet has been initialized
     * (.cf FABRICTHREE-662).
     */
    private class JoinDomainListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                // lookup the component runtime MBean containing the current app deployment state
                ObjectName serverRuntime = (ObjectName) mBeanServer.getAttribute(WLS_RUNTIME_SERVICE_MBEAN, "ServerRuntime");
                ObjectName[] applicationRuntimes = (ObjectName[]) mBeanServer.getAttribute(serverRuntime, "ApplicationRuntimes");
                ObjectName applicationRuntime = null;
                for (ObjectName runtime : applicationRuntimes) {
                    if (runtime.getKeyProperty("Name").contains(FABRIC3_WEBLOGIC_HOST)) {
                        applicationRuntime = runtime;
                        break;
                    }
                }
                if (applicationRuntime == null) {
                    monitor.errorMessage("Application runtime MBean not found. Federation and cluster communication disabled.");
                    return;
                }
                ObjectName[] componentRuntimes = ((ObjectName[]) mBeanServer.getAttribute(applicationRuntime, "ComponentRuntimes"));
                ObjectName componentRuntime = null;
                for (ObjectName runtime : componentRuntimes) {
                    if (runtime.getKeyProperty("ApplicationRuntime").contains(FABRIC3_WEBLOGIC_HOST)) {
                        componentRuntime = runtime;
                        break;
                    }
                }
                if (componentRuntime == null) {
                    monitor.errorMessage("Component runtime MBean not found. Federation and cluster communication disabled.");
                    return;
                }

                int state = (Integer) mBeanServer.getAttribute(componentRuntime, "DeploymentState");

                // If the deployment state is activated, bind immediately. Otherwise, do so asynchronously.
                // Note that an MBean NotificationListener cannot be used as the WLS MBean does not emmit notifications.
                if (WLS_ACTIVATED_STATE == state) {
                    bindController();
                } else {
                    Executors.newSingleThreadExecutor().submit(new DeploymentStateListener(componentRuntime));
                }
            } catch (JMException e) {
                monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);

            }
        }
    }

    /**
     * Binds the controller to the JNDI tree asynchronously.
     */
    private class DeploymentStateListener implements Runnable {
        private ObjectName componentRuntime;

        private DeploymentStateListener(ObjectName componentRuntime) {
            this.componentRuntime = componentRuntime;
        }

        public void run() {
            while (true) {
                try {
                    int state = (Integer) mBeanServer.getAttribute(componentRuntime, "DeploymentState");
                    if (WLS_ACTIVATED_STATE == state) {
                        bindController();
                        return;
                    }
                    // wait one second
                    Thread.sleep(1000);
                } catch (MBeanException e) {
                    monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);
                    return;
                } catch (AttributeNotFoundException e) {
                    monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);
                    return;
                } catch (InstanceNotFoundException e) {
                    monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);
                    return;
                } catch (ReflectionException e) {
                    monitor.errorMessage("Error retrieving deployment state. Federation disabled", e);
                    return;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        }

    }


}
