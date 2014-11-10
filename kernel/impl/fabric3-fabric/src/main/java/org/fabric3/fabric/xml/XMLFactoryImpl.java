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
package org.fabric3.fabric.xml;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Property;

import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.xml.XMLFactoryInstantiationException;

/**
 * An implementation of XMLFactory that uses WoodStox StAX parser for input & output factories. Alternately the runtime can be configured to use a
 * different input and output factories as properties in the scdl file. This implementation sets the TCCL during stAX implementation discovery.
 */
public final class XMLFactoryImpl implements XMLFactory {

    private final String inputFactoryName;
    private final String outputFactoryName;
    private final ClassLoader classLoader = getClass().getClassLoader();

    public XMLFactoryImpl() {
        this("com.ctc.wstx.stax.WstxInputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
    }

    @Constructor
    public XMLFactoryImpl(@Property(name = "input") String inputFactoryName, @Property(name = "output") String outputFactoryName) {
        this.inputFactoryName = inputFactoryName;
        this.outputFactoryName = outputFactoryName;
    }

    /**
     * Creates a new XMLInputFactory instance.
     *
     * @return the XMLInputFactory instance
     * @throws FactoryConfigurationError
     */
    public XMLInputFactory newInputFactoryInstance() throws FactoryConfigurationError {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return newInputFactoryInstance(inputFactoryName, classLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    /**
     * Creates a new XMLOutputFactory instance.
     *
     * @return the XMLOutputFactory instance
     * @throws FactoryConfigurationError
     */
    public XMLOutputFactory newOutputFactoryInstance() throws FactoryConfigurationError {
        return newOutputFactoryInstance(outputFactoryName, classLoader);
    }

    private XMLInputFactory newInputFactoryInstance(String factoryName, ClassLoader cl)
            throws XMLFactoryInstantiationException {
        try {
            Class clazz = cl.loadClass(factoryName);
            return (XMLInputFactory) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new XMLFactoryInstantiationException("Error instantiating factory: " + factoryName, e);
        } catch (ClassNotFoundException e) {
            throw new XMLFactoryInstantiationException("Error loading factory: " + factoryName, e);
        }
    }

    private XMLOutputFactory newOutputFactoryInstance(String factoryName, ClassLoader cl)
            throws FactoryConfigurationError {
        try {
            Class clazz = cl.loadClass(factoryName);
            return (XMLOutputFactory) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new XMLFactoryInstantiationException("Error instantiating factory: " + factoryName, e);
        } catch (ClassNotFoundException e) {
            throw new XMLFactoryInstantiationException("Error loading factory: " + factoryName, e);
        }
    }

}