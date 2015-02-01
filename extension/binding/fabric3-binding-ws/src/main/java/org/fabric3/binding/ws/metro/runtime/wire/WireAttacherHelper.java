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
package org.fabric3.binding.ws.metro.runtime.wire;

import java.security.SecureClassLoader;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Utility methods for wire attachers.
 */
public interface WireAttacherHelper {

    /**
     * Loads an SEI class using either the provided byte array or, if null, the interface name.
     *
     * @param interfaze   the interface name
     * @param classBytes  the byte array. May be null.
     * @param classLoader the classloader to load the class with
     * @return the loaded class
     * @throws Fabric3Exception if the class cannot be loaded
     */
    Class<?> loadSEI(String interfaze, byte[] classBytes, SecureClassLoader classLoader) throws Fabric3Exception;

}
