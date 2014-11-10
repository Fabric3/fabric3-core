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

import java.lang.reflect.InvocationTargetException;
import java.security.SecureClassLoader;

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.ws.metro.util.ClassDefiner;
import org.fabric3.binding.ws.metro.util.ClassLoaderUpdater;

/**
 *
 */
public class WireAttacherHelperImpl implements WireAttacherHelper {
    private ClassDefiner classDefiner;
    private ClassLoaderUpdater classLoaderUpdater;

    public WireAttacherHelperImpl(@Reference ClassDefiner classDefiner, @Reference ClassLoaderUpdater classLoaderUpdater) {
        this.classDefiner = classDefiner;
        this.classLoaderUpdater = classLoaderUpdater;
    }

    public Class<?> loadSEI(String interfaze, byte[] classBytes, SecureClassLoader classLoader) throws ContainerException {
        try {
            Class<?> seiClass;
            if (classBytes != null) {
                seiClass = classDefiner.defineClass(interfaze, classBytes, (SecureClassLoader) classLoader);
            } else {
                // the service interface is not generated
                seiClass = classLoader.loadClass(interfaze);
            }

            classLoaderUpdater.updateClassLoader(seiClass);
            return seiClass;
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            throw new ContainerException(e);
        }
    }

}
