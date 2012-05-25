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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.ws.metro.runtime.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;


/**
 * Invoker that receives a web service invocation from the Metro transport layer and dispatches it through the interceptor chain to a target service
 * that accepts JAXB parameter types.
 *
 * @version $Rev$ $Date$
 */
public class JaxbInvoker extends Invoker {
    private List<BindingHandler<SOAPMessage>> handlers;
    private Map<String, InvocationChain> chains = new HashMap<String, InvocationChain>();

    /**
     * Constructor.
     *
     * @param chains   Invocation chains.
     * @param handlers optional handlers, may be null
     */
    public JaxbInvoker(List<InvocationChain> chains, List<BindingHandler<SOAPMessage>> handlers) {
        this.handlers = handlers;
        for (InvocationChain chain : chains) {
            this.chains.put(chain.getPhysicalOperation().getName(), chain);
        }
    }

    public Object invoke(Packet packet, Method method, Object... args) throws InvocationTargetException {
        // the work context is populated by the current tubeline
        WorkContext workContext = (WorkContext) packet.invocationProperties.get(MetroConstants.WORK_CONTEXT);
        if (workContext == null) {
            // programming error
            throw new AssertionError("Work context not set");
        }

        Message input = new MessageImpl(args, false, workContext);
        Interceptor head = chains.get(method.getName()).getHeadInterceptor();

        invokeHandlers(packet, input);

        Message ret = head.invoke(input);

        if (!ret.isFault()) {
            return ret.getBody();
        } else {
            Throwable th = (Throwable) ret.getBody();
            throw new InvocationTargetException(th);
        }
    }

    /**
     * Overridden as the superclass method throws <code>UnsupportedOperationException</code>
     */
    @Override
    public void start(WebServiceContext wsc) {
    }

    private void invokeHandlers(Packet packet, Message input) throws InvocationTargetException {
        if (handlers != null) {
            try {
                SOAPMessage soapMessage = packet.getMessage().readAsSOAPMessage();
                for (BindingHandler<SOAPMessage> handler : handlers) {
                    handler.handleInbound(soapMessage, input);
                }
            } catch (SOAPException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

}
