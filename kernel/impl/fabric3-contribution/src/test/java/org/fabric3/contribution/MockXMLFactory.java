package org.fabric3.contribution;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.xml.XMLFactoryInstantiationException;

/**
 * @version $Rev$ $Date$
 */
public class MockXMLFactory implements XMLFactory {
    public XMLInputFactory newInputFactoryInstance() throws XMLFactoryInstantiationException {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class clazz = classLoader.loadClass("com.ctc.wstx.stax.WstxInputFactory");
            return (XMLInputFactory) clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new XMLFactoryInstantiationException("Error", e);
        } catch (InstantiationException e) {
            throw new XMLFactoryInstantiationException("Error", e);
        } catch (ClassNotFoundException e) {
            throw new XMLFactoryInstantiationException("Error", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    public XMLOutputFactory newOutputFactoryInstance() throws XMLFactoryInstantiationException {
        return XMLOutputFactory.newInstance();
    }
}