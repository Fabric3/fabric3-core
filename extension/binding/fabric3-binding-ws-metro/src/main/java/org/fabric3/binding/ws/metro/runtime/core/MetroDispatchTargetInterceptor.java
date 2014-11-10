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

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.oasisopen.sca.ServiceRuntimeException;
import org.w3c.dom.Node;

/**
 * Interceptor for invoking a JAX-WS <code>Dispatch</code> instance. Used by invocation chains that dispatch to a web service endpoint defined by a
 * WSDL contract (as opposed to a Java interface).
 * <p/>
 * This interceptor requires message payloads to be a DOM type.
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
     */
    public MetroDispatchTargetInterceptor(MetroDispatchObjectFactory dispatchFactory, boolean oneWay) {
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
                msg.setBody(result.getNode());
                return msg;
            }
        } catch (InaccessibleWSDLException | TransformerException | ObjectCreationException e) {
            throw new ServiceRuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }


}