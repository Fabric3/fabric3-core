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
package org.fabric3.fabric.container.interceptor;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.transform.Transformer;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Converts the input parameters of an invocation to a target format and the output parameters from the target format by delegating to underlying
 * transformers.
 */
public class TransformerInterceptor implements Interceptor {
    private Transformer<Object, Object> inTransformer;
    private Transformer<Object, Object> outTransformer;
    private ClassLoader inLoader;
    private ClassLoader outLoader;
    private Interceptor next;

    /**
     * Constructor.
     *
     * @param inTransformer  the input parameter transformer
     * @param outTransformer the output parameter transformer
     * @param inLoader       the input parameter classloader, i.e. the target service contribution classloader
     * @param outLoader      the output parameter classloader, i.e. the source component contribution classloader
     */
    public TransformerInterceptor(Transformer<Object, Object> inTransformer,
                                  Transformer<Object, Object> outTransformer,
                                  ClassLoader inLoader,
                                  ClassLoader outLoader) {
        this.inTransformer = inTransformer;
        this.outTransformer = outTransformer;
        this.inLoader = inLoader;
        this.outLoader = outLoader;
    }

    public Message invoke(Message msg) {
        transformInput(msg);
        Message ret = next.invoke(msg);
        return transformOutput(ret);
    }

    private void transformInput(Message msg) {
        Object params = msg.getBody();
        // TODO handle null types
        if (params != null) {
            try {
                // Operations take 0..n parameters. A single parameter must be unwrapped from the invocation array and passed to a transformer.
                // In contrast, multiple parameter operations are passed as an array to the transformer.
                if (params.getClass().isArray() && ((Object[]) params).length == 1) {
                    Object[] paramArray = (Object[]) params;
                    paramArray[0] = inTransformer.transform(paramArray[0], inLoader);
                } else {
                    // multiple parameters - pass the entire array to transform
                    Object transformed = inTransformer.transform(params, inLoader);
                    msg.setBody(transformed);
                }
            } catch (ContainerException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    private Message transformOutput(Message ret) {
        // FIXME For exception transformation, if it is checked convert as application fault
        Object body = ret.getBody();
        // TODO handle null types
        if (body != null) {
            try {
                Object transformed = outTransformer.transform(body, outLoader);
                if (ret.isFault()) {
                    ret.setBodyWithFault(transformed);
                } else {
                    ret.setBody(transformed);
                }
            } catch (ClassCastException e) {
                // an unexpected type was returned by the target service or an interceptor later in the chain. This is an error in the extension or
                // interceptor and not user code since errors should be trapped and returned in the format expected by the transformer
                if (body instanceof Throwable) {
                    throw new ServiceRuntimeException("Unexpected exception returned", (Throwable) body);
                } else {
                    throw new ServiceRuntimeException("Unexpected type returned: " + body.getClass());
                }
            } catch (ContainerException e) {
                throw new ServiceRuntimeException(e);
            }
        }
        return ret;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }

}
