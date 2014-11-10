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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ListIterator;
import java.util.Stack;
import java.util.UUID;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

import org.fabric3.api.host.util.IOHelper;

/**
 * Returns an <code>InputSource</code> for an imported schema. Resolution is done by interpreting the <code>schemaLocation</code> attribute to be
 * relative to the base URL if the importing document.
 * <p/>
 * This implementation introduces a number of hacks to work around the default behavior of Apache XmlSchema. First, the base URI used by XmlSchema is
 * fixed as the URL of the original document. If an imported schema located in a different directory imports another schema that is specified using a
 * relative location, the default behavior of XmlSchema is to resolve the second schema against the base WSDL location, which is incorrect.
 * <p/>
 * This resolver changes that behavior by resolving schemas relative to their importing document. This is done by traversing the stack of importing
 * documents and building the schema URL relative to the location of those documents.
 */
public class RelativeUrlResolver implements URIResolver {
    private static Field schemaKeyIdField;

    private URIResolver next;
    private XmlSchemaCollection collection;
    private Field stackField;

    public RelativeUrlResolver(XmlSchemaCollection collection, URIResolver next) {
        this.collection = collection;
        this.next = next;
        try {
            this.stackField = XmlSchemaCollection.class.getDeclaredField("stack");
            this.stackField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        String base = getBase(baseUri);
        try {
            URL url = new URL(new URL(base), schemaLocation);
            InputStream stream = null;
            try {
                stream = url.openStream();
                if (stream != null) {
                    return (createSource(url));
                }
            } catch (IOException e) {
                // file not found
                return next.resolveEntity(targetNamespace, schemaLocation, baseUri);

            } finally {
                IOHelper.closeQuietly(stream);
            }
        } catch (IOException e) {
            e.printStackTrace(); // should not happen
        }
        return next.resolveEntity(targetNamespace, schemaLocation, baseUri);
    }

    /**
     * Returns the base URL for an imported schema by transitively resolving the chain of imports (one imported document importing another).
     *
     * @param baseUri the base importing document URL
     * @return the base URL
     */
    private String getBase(String baseUri) {
        int pos = baseUri.indexOf("#");
        String base = baseUri;
        if (pos > 0) {
            base = baseUri.substring(0, pos);
        }

        Stack stack = getStack();
        URI uri = URI.create(base);
        if (!stack.isEmpty()) {
            int index = stack.size() - 1;
            ListIterator iterator = stack.listIterator(stack.size());
            while (iterator.hasPrevious()) {
                if (index == 0) {
                    break;
                }
                Object key = iterator.previous();
                uri = buildUri(base, key);
                index--;
            }

        }
        if (uri != null) {
            return uri.toString();
        }
        return base;
    }

    /**
     * Resolves a URI against a base, taking special care of the JAR scheme (URL.resolve() has trouble with it).
     *
     * @param base the base URI
     * @param key  the schema key, used to obtain the relative URL of the schema
     * @return the resolved URI
     */
    private URI buildUri(String base, Object key) {
        String current = getSystemId(key);
        current = current.substring(0, current.indexOf("#"));
        String schemeBase = URI.create(base).getSchemeSpecificPart();
        String relativeScheme = URI.create(current).getSchemeSpecificPart();
        return URI.create("jar:" + URI.create(schemeBase).resolve(relativeScheme).toString());
    }

    /**
     * Hack to get access the the systemId (relative URL) of a schema. XmlSchema does not provide public access to the field.
     *
     * @param key the schema key
     * @return the system id
     */
    private String getSystemId(Object key) {
        try {
            if (schemaKeyIdField == null) {
                schemaKeyIdField = key.getClass().getDeclaredField("systemId");
                schemaKeyIdField.setAccessible(true);
            }
            return (String) schemaKeyIdField.get(key);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private InputSource createSource(URL url) {
        try {
            InputSource source = new InputSource(url.openStream());
            // encode the system ID to work around an XmlSchema bug where an exception is thrown if a document is imported more than once when
            // transitively resolving imports.
            source.setSystemId(url.toString() + "#" + UUID.randomUUID().toString());
            return source;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the stack of transitive imports.
     *
     * @return the stack
     */
    private Stack getStack() {
        try {
            return (Stack) stackField.get(collection);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}