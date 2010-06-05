/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.xml;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Property;

import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.xml.XMLFactoryInstantiationException;

/**
 * An implementation of XMLFactory that uses WoodStox StAX parser for input & output factories. Alternately the runtime can be configured to use a
 * different input and ouput factories as properties in the scdl file. This implementation sets the TCCL during stAX implementation discovery.
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
        } catch (InstantiationException e) {
            throw new XMLFactoryInstantiationException("Error instantiating factory: " + factoryName, e);
        } catch (IllegalAccessException e) {
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
        } catch (InstantiationException e) {
            throw new XMLFactoryInstantiationException("Error instantiating factory: " + factoryName, e);
        } catch (IllegalAccessException e) {
            throw new XMLFactoryInstantiationException("Error instantiating factory: " + factoryName, e);
        } catch (ClassNotFoundException e) {
            throw new XMLFactoryInstantiationException("Error loading factory: " + factoryName, e);
        }
    }

}