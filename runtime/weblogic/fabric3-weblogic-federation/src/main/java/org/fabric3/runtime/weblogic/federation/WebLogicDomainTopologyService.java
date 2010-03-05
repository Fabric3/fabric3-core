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
package org.fabric3.runtime.weblogic.federation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.runtime.weblogic.cluster.ChannelException;
import org.fabric3.runtime.weblogic.cluster.RuntimeChannel;
import static org.fabric3.runtime.weblogic.federation.Constants.CONTROLLER_CHANNEL;
import static org.fabric3.runtime.weblogic.federation.Constants.PARTICIPANT_CONTEXT;
import org.fabric3.spi.classloader.SerializationService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.ResponseCommand;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.Response;
import org.fabric3.spi.federation.RuntimeInstance;

/**
 * Provides domain-wide controller communication using the WebLogic clustered JNDI tree.
 *
 * @version $Rev$ $Date$
 */
@Service(DomainTopologyService.class)
@EagerInit
public class WebLogicDomainTopologyService implements DomainTopologyService {
    private String runtimeName = "controller";
    private CommandExecutorRegistry executorRegistry;
    private EventService eventService;
    private SerializationService serializationService;
    private WebLogicTopologyMonitor monitor;
    private InitialContext rootContext;
    private EventContext participantContext;

    public WebLogicDomainTopologyService(@Reference CommandExecutorRegistry executorRegistry,
                                         @Reference EventService eventService,
                                         @Reference SerializationService serializationService,
                                         @Monitor WebLogicTopologyMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.eventService = eventService;
        this.serializationService = serializationService;
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

    public List<String> getZones() {
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
                RuntimeInstance runtimeInstance = new RuntimeInstance(runtimeName);
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
        } catch (NamingException e) {
            throw new MessageException(e);
        }
    }

    private class JoinDomainListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            try {
                RuntimeChannel controllerChannel = new RuntimeChannelImpl(runtimeName, executorRegistry, serializationService, monitor);
                Hashtable<String, String> env = new Hashtable<String, String>();
                rootContext = new InitialContext(env);
                Context controllerContext = JndiHelper.getContext(Constants.CONTROLLER_CONTEXT, rootContext);
                try {
                    controllerContext.bind(CONTROLLER_CHANNEL, controllerChannel);
                } catch (NameAlreadyBoundException e) {
                    controllerContext.rebind(CONTROLLER_CHANNEL, controllerChannel);
                }
                Context ctx = JndiHelper.getContext(PARTICIPANT_CONTEXT, rootContext);
                assert ctx instanceof EventContext;
                participantContext = (EventContext) ctx;
                ParticipantContextListener listener = new ParticipantContextListener();
                participantContext.addNamingListener("", EventContext.ONELEVEL_SCOPE, listener);
            } catch (NamingException e) {
                monitor.errorMessage("Error initializing domain topology service", e);
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }


    private class ParticipantContextListener implements NamespaceChangeListener {

        public void namingExceptionThrown(NamingExceptionEvent evt) {
            // no-op
        }

        public void objectAdded(NamingEvent evt) {
            // TODO update zone metadata
        }

        public void objectRemoved(NamingEvent evt) {
            // TODO update zone metadata
        }

        public void objectRenamed(NamingEvent evt) {

        }
    }


}
