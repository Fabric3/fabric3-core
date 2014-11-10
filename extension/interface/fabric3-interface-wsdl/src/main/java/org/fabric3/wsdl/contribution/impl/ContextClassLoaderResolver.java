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
package org.fabric3.wsdl.contribution.impl;

import java.io.IOException;
import java.net.URL;

import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

/**
 * Resolves a WSDL entity by first using the thread context classloader and if not found, a default URI resolver. The following resource locations
 * will be used with the thread context classloader: <ul>
 * <p/>
 * <li>Root jar directory
 * <p/>
 * <li>wsdl directory
 * <p/>
 * <li>META-INF directory
 * <p/>
 * <li>META-INF/wsdl directory
 * <p/>
 * </ul>
 */
public class ContextClassLoaderResolver implements URIResolver {
    private URIResolver next;

    public ContextClassLoaderResolver(URIResolver next) {
        this.next = next;
    }

    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(schemaLocation);
        if (url != null) {
            return createSource(url);
        }
        url = loader.getResource("wsdl/" + schemaLocation);
        if (url != null) {
            return createSource(url);
        }
        url = loader.getResource("META-INF/" + schemaLocation);
        if (url != null) {
            return createSource(url);
        }
        url = loader.getResource("META-INF/wsdl/" + schemaLocation);
        if (url != null) {
            return createSource(url);
        }
        return next.resolveEntity(targetNamespace, schemaLocation, baseUri);
    }

    private InputSource createSource(URL url) {
        try {
            return new InputSource(url.openStream());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}