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
package org.fabric3.api.host.runtime;

/**
 * Creates BootstrapService instances.
 */
public class BootstrapFactory {
    private static final String FACTORY_CLASS = "org.fabric3.fabric.runtime.DefaultBootstrapService";

    /**
     * Returns a BootstrapService for the given classloader.
     *
     * @param bootClassLoader the classloader the BootstrapService should be loaded by
     * @return a BootstrapService
     */
    public static BootstrapService getService(ClassLoader bootClassLoader) {
        try {
            Class<?> implClass = Class.forName(FACTORY_CLASS, true, bootClassLoader);
            return (BootstrapService) implClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // programming error
            throw new AssertionError(e);
        }
    }

}