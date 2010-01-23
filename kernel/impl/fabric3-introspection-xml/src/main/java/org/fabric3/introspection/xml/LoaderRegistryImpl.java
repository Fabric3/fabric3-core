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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;
import org.fabric3.spi.xml.XMLFactory;

/**
 * The default implementation of a loader registry
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LoaderRegistryImpl implements LoaderRegistry {
    private final XMLInputFactory xmlFactory;
    private Map<QName, TypeLoader<?>> mappedLoaders;
    private final Map<QName, TypeLoader<?>> loaders = new HashMap<QName, TypeLoader<?>>();

    public LoaderRegistryImpl(@Reference XMLFactory factory) {
        this.xmlFactory = factory.newInputFactoryInstance();
    }

    @Reference(required = false)
    public void setLoaders(Map<QName, TypeLoader<?>> mappedLoaders) {
        this.mappedLoaders = mappedLoaders;
    }

    public void registerLoader(QName element, TypeLoader<?> loader) {
        if (loaders.containsKey(element)) {
            throw new IllegalStateException("Loader already registered for " + element);
        }
        loaders.put(element, loader);
    }

    public void unregisterLoader(QName element) {
        loaders.remove(element);
    }

    public <O> O load(XMLStreamReader reader, Class<O> type, IntrospectionContext introspectionContext)
            throws XMLStreamException, UnrecognizedElementException {
        QName name = reader.getName();
        TypeLoader<?> loader = loaders.get(name);
        if (loader == null) {
            loader = mappedLoaders.get(name);
        }
        if (loader == null) {
            throw new UnrecognizedElementException(reader);
        }
        return type.cast(loader.load(reader, introspectionContext));
    }

    public <O> O load(URL url, Class<O> type, IntrospectionContext ctx) throws LoaderException {
        InputStream stream;
        try {
            stream = url.openStream();
        } catch (IOException e) {
            throw new LoaderException("Invalid URL: " + url.toString(), e);
        }
        try {
            try {
                return load(url, stream, type, ctx);
            } catch (XMLStreamException e) {
                throw new LoaderException("Invalid URL: " + url.toString(), e);
            }
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private <O> O load(URL url, InputStream stream, Class<O> type, IntrospectionContext ctx) throws XMLStreamException, UnrecognizedElementException {
        XMLStreamReader reader;
        reader = xmlFactory.createXMLStreamReader(url.toString(), stream);

        try {
            reader.nextTag();
            return load(reader, type, ctx);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // ignore
                }
            }
        }
    }

}
