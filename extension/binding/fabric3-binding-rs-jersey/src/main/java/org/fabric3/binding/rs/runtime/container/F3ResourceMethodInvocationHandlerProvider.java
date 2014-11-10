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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodInvocationHandlerProvider;
import org.jvnet.hk2.annotations.Contract;

/**
 * Passes an invocation from Jersey to Fabric3.
 */
@Contract
public class F3ResourceMethodInvocationHandlerProvider implements ResourceMethodInvocationHandlerProvider {
    private static final Handler HANDLER = new Handler();

    public InvocationHandler create(Invocable method) {
        return HANDLER;
    }

    private static class Handler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return ((F3ResourceHandler) proxy).invoke(method, args);
        }
    }
}
