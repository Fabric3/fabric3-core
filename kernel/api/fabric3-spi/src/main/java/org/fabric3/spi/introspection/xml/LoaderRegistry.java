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
package org.fabric3.spi.introspection.xml;

import javax.xml.namespace.QName;

/**
 * Registry for XML loaders that can parse a StAX input stream and return model objects. Loaders will typically be contributed to the runtime by any extension
 * that needs to handle extension specific information contained in an XML file.
 */
public interface LoaderRegistry extends Loader {
    /**
     * Register a loader. This operation will typically be called by a loader during its initialization.
     *
     * @param element the name of the XML global element that this loader can handle
     * @param loader  a loader that is being contributed to the system
     * @throws IllegalStateException if there is already a loader registered for the supplied element
     */
    void registerLoader(QName element, TypeLoader<?> loader) throws IllegalStateException;

    /**
     * Unregister the loader for the supplied element. This will typically be called by a loader as it is being destroyed. This method simply returns if no
     * loader is registered for that element.
     *
     * @param element the element whose loader should be unregistered
     */
    void unregisterLoader(QName element);

    /**
     * Returns true if the element is registered.
     *
     * @param element the element
     * @return true if the element is registered
     */
    boolean isRegistered(QName element);
}
