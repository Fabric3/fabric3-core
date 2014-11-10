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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.reflection.jdk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;

/**
 * Performs an invocation on a method of a given target instance
 */
public class ConsumerMethodInvoker implements ConsumerInvoker {
    private final Method method;

    /**
     * Instantiates an invoker for the given method.
     *
     * @param method the method to invoke on
     */
    public ConsumerMethodInvoker(Method method) {
        this.method = method;
        this.method.setAccessible(true);
    }

    public Object invoke(Object obj, Object args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(obj, args);
    }
}
