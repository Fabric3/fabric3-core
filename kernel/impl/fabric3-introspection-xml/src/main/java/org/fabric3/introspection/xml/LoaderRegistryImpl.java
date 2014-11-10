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
package org.fabric3.introspection.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.stream.Source;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.xml.XMLFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * The default implementation of a loader registry
 */
@EagerInit
public class LoaderRegistryImpl implements LoaderRegistry {
    private final XMLInputFactory xmlFactory;
    private Map<QName, TypeLoader<?>> mappedLoaders = new HashMap<>();
    private final Map<QName, TypeLoader<?>> loaders = new HashMap<>();

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

    public boolean isRegistered(QName element) {
        return mappedLoaders.containsKey(element) || loaders.containsKey(element);
    }

    public <O> O load(XMLStreamReader reader, Class<O> type, IntrospectionContext introspectionContext) throws XMLStreamException {
        QName name = reader.getName();
        TypeLoader<?> loader = loaders.get(name);
        if (loader == null) {
            loader = mappedLoaders.get(name);
        }
        if (loader == null) {
            UnrecognizedElement failure = new UnrecognizedElement(reader, reader.getLocation());
            introspectionContext.addError(failure);
            return null;
        }
        return type.cast(loader.load(reader, introspectionContext));
    }

    public <O> O load(Source source, Class<O> type, IntrospectionContext ctx) throws LoaderException {
        String id = source.getSystemId();
        InputStream stream;
        try {
            stream = source.openStream();
        } catch (IOException e) {
            throw new LoaderException("Invalid source: " + id, e);
        }
        try {
            try {
                return load(id, stream, type, ctx);
            } catch (XMLStreamException e) {
                throw new LoaderException("Invalid source: " + id, e);
            }
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private <O> O load(String id, InputStream stream, Class<O> type, IntrospectionContext ctx) throws XMLStreamException {
        XMLStreamReader reader;
        // if the id is a URL, use it as the system id
        if (isURL(id)) {
            reader = xmlFactory.createXMLStreamReader(id, stream);
        } else {
            reader = xmlFactory.createXMLStreamReader(stream);
        }

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

    /**
     * Tests to see if a string can be parsed as a URL
     *
     * @param path the string as a path
     * @return true if the string can be parsed as a URL
     */
    private boolean isURL(String path) {
        try {
            new URL(path);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }
}
