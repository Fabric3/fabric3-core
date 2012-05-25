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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Interceptor for invoking a JAX-WS proxy generated from a Java interface.  Used by invocation chains that dispatch to a web service endpoint defined
 * by a Java interface (as opposed to a WSDL contract).
 * <p/>
 * This interceptor requires message payloads to be a JAXB types.
 *
 * @version $Rev$ $Date$
 */
public class MetroJavaTargetInterceptor extends AbstractMetroTargetInterceptor {
    private ObjectFactory<?> proxyFactory;
    private Method method;
    private boolean oneWay;
    private int retries = 0;
    private InterceptorMonitor monitor;

    /**
     * Constructor.
     *
     * @param proxyFactory            the service proxy factory
     * @param method                  method corresponding to the invoked operation
     * @param oneWay                  true if the operation is non-blocking
     * @param retries                 the number of retries to attempt if the service is unavailable
     * @param monitor                 the monitor
     */
    public MetroJavaTargetInterceptor(ObjectFactory<?> proxyFactory,
                                      Method method,
                                      boolean oneWay,
                                      int retries,
                                      InterceptorMonitor monitor) {
        this.proxyFactory = proxyFactory;
        this.method = method;
        this.oneWay = oneWay;
        this.retries = retries;
        this.monitor = monitor;
    }

    public Message invoke(Message msg) {
        Object[] payload = (Object[]) msg.getBody();
        Object proxy = createProxy();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Metro stubs attempt to load classes using TCCL (e.g. StAX provider classes) that are visible to the extension classloader and not
            // visible to the application classloader.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return invokeRetry(payload, proxy);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Invokes the web service proxy, retrying a configured number of times if the service is unavailable.
     *
     * @param payload the web service in parameters
     * @param proxy   the web service proxy
     * @return the web service out parameters or null
     */
    private Message invokeRetry(Object[] payload, Object proxy) {
        int retry = 0;
        while (true) {
            try {
                if (oneWay) {
                    method.invoke(proxy, payload);
                    return NULL_RESPONSE;
                } else {
                    Object ret = method.invoke(proxy, payload);
                    return new MessageImpl(ret, false, null);
                }
            } catch (WebServiceException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    if (retry > retries) {
                        throw new ServiceUnavailableException(e);
                    }
                    monitor.serviceUnavailableRetry(e);
                    ++retry;
                } else {
                    throw new ServiceRuntimeException(e);
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof WebServiceException && !(e.getTargetException() instanceof SOAPFaultException)) {
                    WebServiceException wse = (WebServiceException) e.getTargetException();
                    if (wse.getCause() instanceof SocketTimeoutException) {
                        if (retry >= retries) {
                            throw new ServiceUnavailableException(e);
                        }
                        monitor.serviceUnavailableRetry(wse.getCause());
                        ++retry;
                    } else {
                        throw new ServiceRuntimeException(e);
                    }
                } else {
                    return new MessageImpl(e.getTargetException(), true, null);
                }
            }
        }
    }

    private Object createProxy() {
        try {
            return proxyFactory.getInstance();
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
