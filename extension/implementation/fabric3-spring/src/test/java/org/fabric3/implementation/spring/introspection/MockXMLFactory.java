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
package org.fabric3.implementation.spring.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.xml.XMLFactoryInstantiationException;

/**
 *
 */
public class MockXMLFactory implements XMLFactory {
    public XMLInputFactory newInputFactoryInstance() throws XMLFactoryInstantiationException {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class clazz = classLoader.loadClass("com.ctc.wstx.stax.WstxInputFactory");
            return (XMLInputFactory) clazz.newInstance();
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            throw new XMLFactoryInstantiationException("Error", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    public XMLOutputFactory newOutputFactoryInstance() throws XMLFactoryInstantiationException {
        return XMLOutputFactory.newInstance();
    }
}