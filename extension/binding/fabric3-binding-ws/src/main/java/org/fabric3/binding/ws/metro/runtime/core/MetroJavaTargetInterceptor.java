/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.binding.ws.metro.runtime.core;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * Interceptor for invoking a JAX-WS proxy generated from a Java interface.  Used by invocation chains that dispatch to a web service endpoint defined
 * by a Java interface (as opposed to a WSDL contract).
 * <p/>
 * This interceptor requires message payloads to be a JAXB types.
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
        Object proxy = createProxy();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Metro stubs attempt to load classes using TCCL (e.g. StAX provider classes) that are visible to the extension classloader and not
            // visible to the application classloader.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return invokeRetry(msg, proxy);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Invokes the web service proxy, retrying a configured number of times if the service is unavailable.
     *
     * @param msg theincoming message
     * @param proxy   the web service proxy
     * @return the web service out parameters or null
     */
    private Message invokeRetry(Message msg, Object proxy) {
        int retry = 0;
        Object[] payload = (Object[]) msg.getBody();
        while (true) {
            try {
                if (oneWay) {
                    method.invoke(proxy, payload);
                    return NULL_RESPONSE;
                } else {
                    Object ret = method.invoke(proxy, payload);
                    msg.setBody(ret);
                    return msg;
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
                    msg.setBodyWithFault(e.getTargetException());
                    return msg;
                }
            }
        }
    }

    private Object createProxy() {
        try {
            return proxyFactory.getInstance();
        } catch (ContainerException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
