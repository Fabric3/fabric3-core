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
package org.fabric3.binding.rs.runtime.container;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 *
 */
public class RsClientInterceptor implements Interceptor {
    private RsClientResponse response;

    public RsClientInterceptor(String operName, Class<?> interfaze, URI uri, List<Class<?>> classes) throws Exception {
        response = createResponseConfiguration(uri, interfaze, operName, classes);
    }

    public Message invoke(Message message) {
        Object[] args = (Object[]) message.getBody();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Object body = response.build(args);
            message.reset();
            message.setBody(body);
        } catch (RuntimeException e) {
            throw new ServiceRuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        return message;
    }

    public void setNext(Interceptor interceptor) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }

    private RsClientResponse createResponseConfiguration(URI uri, Class<?> interfaze, String operation, List<Class<?>> args) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Method m = interfaze.getMethod(operation, args.toArray(new Class[args.size()]));
            RsClientResponse cfg = new RsClientResponse(m.getReturnType(), uri);
            cfg = cfg.
                    // Class level
                            withPath(interfaze.getAnnotation(Path.class)).
                    withProduces(interfaze.getAnnotation(Produces.class)).
                    withConsumes(interfaze.getAnnotation(Consumes.class)).
                    // Method level overriding
                            withAction(m.getAnnotation(PUT.class)).
                    withAction(m.getAnnotation(POST.class)).
                    withAction(m.getAnnotation(GET.class)).
                    withPath(m.getAnnotation(Path.class)).
                    withProduces(m.getAnnotation(Produces.class)).
                    withConsumes(m.getAnnotation(Consumes.class));
            Annotation[][] parameterAnnotations = m.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                cfg.withParam(i, parameterAnnotations[i]);
            }
            return cfg;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
