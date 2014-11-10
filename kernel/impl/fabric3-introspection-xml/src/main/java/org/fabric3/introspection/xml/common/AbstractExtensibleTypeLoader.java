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
package org.fabric3.introspection.xml.common;

import javax.xml.namespace.QName;

import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;

/**
 * TypeLoader implementation that can delegate back to the LoaderRegistry to process sub-elements in a composite.
 */
@EagerInit
public abstract class AbstractExtensibleTypeLoader<T> extends AbstractValidatingTypeLoader<T> {
    protected LoaderRegistry registry;

    protected AbstractExtensibleTypeLoader(LoaderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.registerLoader(getXMLType(), this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(getXMLType());
    }

    /**
     * Returns the QName of the type this implementation loads.
     *
     * @return the QName of the type this implementation loads
     */
    public abstract QName getXMLType();

}
