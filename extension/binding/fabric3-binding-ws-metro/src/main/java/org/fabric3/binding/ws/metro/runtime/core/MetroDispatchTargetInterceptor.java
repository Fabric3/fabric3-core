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
package org.fabric3.binding.ws.metro.runtime.core;

import java.util.Map;

import javax.xml.rpc.handler.HandlerRegistry;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import org.oasisopen.sca.ServiceRuntimeException;
import org.w3c.dom.Node;

import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.spi.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.objectfactory.ObjectCreationException;

/**
 * Interceptor for invoking a JAX-WS <code>Dispatch</code> instance. Used by invocation chains that dispatch to a web service endpoint defined by a
 * WSDL contract (as opposed to a Java interface).
 * <p>
 * This interceptor requires message payloads to be a DOM type.
 *
 * @version $Rev: 7476 $ $Date: 2009-08-15 01:25:27 -0400 (Sat, 15 Aug 2009) $
 */
public class MetroDispatchTargetInterceptor extends AbstractMetroTargetInterceptor {
    private MetroDispatchObjectFactory proxyFactory;
    private boolean oneWay;
    private TransformerFactory transformerFactory;

    /**
     * Constructor.
     *
     * @param dispatchFactory         the factory that creates the JAX-WS Dispatch instance for the reference
     * @param oneWay                  true if the operation is non-blocking
     * @param securityConfiguration   the security configuration or null if security is not configured
     * @param connectionConfiguration the underlying HTTP connection configuration or null if defaults should be used
     * @param handlerRegistry         the global binding handler registry
     */
    public MetroDispatchTargetInterceptor(MetroDispatchObjectFactory dispatchFactory,
                                          boolean oneWay,
                                          SecurityConfiguration securityConfiguration,
                                          ConnectionConfiguration connectionConfiguration, 
                                          BindingHandlerRegistry handlerRegistry) {
        super(securityConfiguration, connectionConfiguration, handlerRegistry);
        this.proxyFactory = dispatchFactory;
        this.oneWay = oneWay;
        transformerFactory = TransformerFactory.newInstance();
    }

    public Message invoke(Message msg) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Object[] payload = (Object[]) msg.getBody();
            if (payload.length != 1) {
                throw new IllegalArgumentException("Payload must contain a single parameter");
            } else if (!(payload[0] instanceof Node)) {
                throw new IllegalArgumentException("Payload must be a Node");
            }
            Node parameter = (Node) payload[0];
            Dispatch<Source> dispatch = proxyFactory.getInstance();
            configureSecurity(dispatch);
            configureConnection(dispatch);
            loadHandlers(dispatch);
            setSOAPAction(dispatch);
            Source source = new DOMSource(parameter);

            // Metro attempts to load classes using TCCL (e.g. StAX provider classes) that are visible the extension classloader and not
            // visible to the application classloader.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            if (oneWay) {
                dispatch.invokeAsync(source, null);
                return NULL_RESPONSE;
            } else {
                Object ret = dispatch.invoke(source);
                if (!(ret instanceof Source)) {
                    throw new ServiceRuntimeException("Returned object must be a " + Source.class.getName());
                }
                Source returnSource = (Source) ret;
                Transformer transformer = transformerFactory.newTransformer();
                DOMResult result = new DOMResult();
                transformer.transform(returnSource, result);
                return new MessageImpl(result.getNode(), false, null);
            }
        } catch (InaccessibleWSDLException e) {
            throw new ServiceRuntimeException(e);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e);
        } catch (TransformerException e) {
            throw new ServiceRuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    //FIXxME
    private void setSOAPAction(Dispatch<Source> dispatch) {
        Map<String, Object> context = dispatch.getRequestContext();
        context.put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        context.put(BindingProvider.SOAPACTION_URI_PROPERTY, "");
    }

}