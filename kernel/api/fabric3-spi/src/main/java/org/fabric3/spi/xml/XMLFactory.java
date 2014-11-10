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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * Provides instances of XMLInputFactory and XMLOutputFactory. This service provides classloading semantics and works around a bug in the JDK StAX
 * parser API (StAX 1.0) which returns an XMLInputFactory for XMLOutputFactory.newInstance(String,ClassLoader).
 */

public interface XMLFactory {

    /**
     * Return the runtime's XMLInputFactory implementation.
     *
     * @return the factory
     * @throws XMLFactoryInstantiationException
     *          if an error occurs loading the factory
     */
    XMLInputFactory newInputFactoryInstance() throws XMLFactoryInstantiationException;

    /**
     * Return the runtime's XMLOutputFactory implementation.
     *
     * @return the factory
     * @throws XMLFactoryInstantiationException
     *          if an error occurs loading the factory
     */
    XMLOutputFactory newOutputFactoryInstance() throws XMLFactoryInstantiationException;

}
