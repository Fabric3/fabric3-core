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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.implementation.pojo.component.ConversationImpl;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.component.InstanceInvocationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Dispatches from a proxy to a wire.
 *
 * @version $Rev$ $Date$
 */
public final class JDKInvocationHandler<B> implements ConversationExpirationCallback, InvocationHandler, ServiceReference<B> {
    private static final long serialVersionUID = -5841336280391145583L;
    private final Class<B> businessInterface;
    private final B proxy;
    private final InteractionType type;
    private final Map<Method, InvocationChain> chains;
    private final ScopeContainer scopeContainer;

    private F3Conversation conversation;
    private String callbackUri;

    /**
     * Constructor.
     *
     * @param interfaze   the proxy interface
     * @param callbackUri the callback uri or null if the wire is unidirectional
     * @param mapping     the method to invocation chain mappings for the wire
     * @throws NoMethodForOperationException if an error occurs creating the proxy
     */
    public JDKInvocationHandler(Class<B> interfaze, String callbackUri, Map<Method, InvocationChain> mapping) throws NoMethodForOperationException {
        this(interfaze, InteractionType.STATELESS, callbackUri, mapping, null);
    }

    /**
     * Constructor.
     *
     * @param interfaze      the proxy interface
     * @param type           the interaction style for the wire
     * @param callbackUri    the callback uri or null if the wire is unidirectional
     * @param mapping        the method to invocation chain mappings for the wire
     * @param scopeContainer the conversational scope container
     * @throws NoMethodForOperationException if an error occurs creating the proxy
     */
    public JDKInvocationHandler(Class<B> interfaze,
                                InteractionType type,
                                String callbackUri,
                                Map<Method, InvocationChain> mapping,
                                ScopeContainer scopeContainer) throws NoMethodForOperationException {
        this.callbackUri = callbackUri;
        assert mapping != null;
        this.businessInterface = interfaze;
        ClassLoader loader = interfaze.getClassLoader();
        this.proxy = interfaze.cast(Proxy.newProxyInstance(loader, new Class[]{interfaze}, this));
        this.chains = mapping;
        this.scopeContainer = scopeContainer;
        this.type = type;
    }


    public void expire(F3Conversation conversation) {
        this.conversation = null;
    }

    public B getService() {
        return proxy;
    }

    public Class<B> getBusinessInterface() {
        return businessInterface;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }

        Interceptor headInterceptor = chain.getHeadInterceptor();
        assert headInterceptor != null;

        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        CallFrame frame = initalizeCallFrame(workContext);
        Message msg = new MessageImpl();
        msg.setBody(args);
        msg.setWorkContext(workContext);
        try {
            // dispatch the invocation down the chain and get the response
            Message resp;
            try {
                resp = headInterceptor.invoke(msg);
            } catch (ServiceUnavailableException e) {
                // simply rethrow ServiceUnavailableExceptions
                throw e;
            } catch (ServiceRuntimeException e) {
                // simply rethrow ServiceRuntimeException
                throw e;
            } catch (org.osoa.sca.ServiceUnavailableException e) {
                // rethrow OSOA ServiceUnavailableExceptions
                throw e;
            } catch (RuntimeException e) {
                // wrap other exceptions raised by the runtime
                throw new ServiceUnavailableException(e);
            }

            // handle response from the application, returning or throwing an exception as appropriate
            Object body = resp.getBody();
            if (resp.isFault()) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            if (InteractionType.CONVERSATIONAL == type || InteractionType.PROPAGATES_CONVERSATION == type) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                if (operation.isEndsConversation()) {
                    conversation = null;
                }
            }
            if (frame != null) {
                // no callframe was created as the wire is unidrectional and non-conversational 
                workContext.popCallFrame();
            }
        }

    }

    public ServiceReference<B> getServiceReference() {
        return this;
    }

    /**
     * Initializes and returns a CallFrame for the invocation if it is required. A CallFrame is required if the wire is targeted to a conversational
     * service or is bidrectional (i.e. there is a callback). It is not required if the wire is targeted to a unidirectional, non-conversational
     * service. If not required, null is returned, thereby avoiding the overhead of creating and pushing a CallFrame onto the current WorkContext.
     *
     * @param workContext the current work context
     * @return a CallFrame for the invocation or null if none is required.
     */
    private CallFrame initalizeCallFrame(WorkContext workContext) {
        CallFrame frame = null;
        if (InteractionType.CONVERSATIONAL == type && conversation == null) {
            conversation = new ConversationImpl(createConversationID(), scopeContainer);
            // register this proxy to receive notifications when the conversation ends
            scopeContainer.registerCallback(conversation, this);
            // mark the CallFrame as starting a conversation
            frame = new CallFrame(callbackUri, null, conversation, ConversationContext.NEW);
            workContext.addCallFrame(frame);
        } else if (InteractionType.PROPAGATES_CONVERSATION == type && conversation == null) {
            F3Conversation propagated = workContext.peekCallFrame().getConversation();
            frame = new CallFrame(callbackUri, null, propagated, ConversationContext.PROPAGATE);
            workContext.addCallFrame(frame);
        } else if (InteractionType.CONVERSATIONAL == type) {
            frame = new CallFrame(callbackUri, null, conversation, null);
            workContext.addCallFrame(frame);
        } else if (callbackUri != null) {
            // the wire is bidrectional so a callframe is required
            frame = new CallFrame(callbackUri, null, null, null);
            workContext.addCallFrame(frame);
        }
        return frame;
    }

    /**
     * Creates a new conversational id
     *
     * @return the conversational id
     */
    private Object createConversationID() {
        return UUID.randomUUID().toString();
    }

    private Object handleProxyMethod(Method method) throws InstanceInvocationException {
        if (method.getParameterTypes().length == 0 && "toString".equals(method.getName())) {
            return "[Proxy - " + Integer.toHexString(hashCode()) + "]";
        } else if (method.getDeclaringClass().equals(Object.class)
                && "equals".equals(method.getName())) {
            // TODO implement
            throw new UnsupportedOperationException();
        } else if (Object.class.equals(method.getDeclaringClass())
                && "hashCode".equals(method.getName())) {
            return hashCode();
            // TODO beter hash algorithm
        }
        String op = method.getName();
        throw new InstanceInvocationException("Operation not configured: " + op, op);
    }
}
