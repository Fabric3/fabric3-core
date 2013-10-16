/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.spi.introspection.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.ModelObject;

/**
 * Utility functions to support loader implementations.
 */
public final class LoaderUtil {
    private LoaderUtil() {
    }

    /**
     * Advance the stream to the next END_ELEMENT event skipping any nested content.
     *
     * @param reader the reader to advance
     * @throws XMLStreamException if there was a problem reading the stream
     */
    public static void skipToEndElement(XMLStreamReader reader) throws XMLStreamException {
        int depth = 0;
        while (true) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (depth == 0) {
                    return;
                }
                depth--;
            }
        }
    }

    /**
     * Construct a QName from an XML value.
     *
     * @param text             the text of an XML QName; if null or "" then null will be returned
     * @param defaultNamespace the default namespace to use if none is defined
     * @param context          the context for resolving namespace prefixes
     * @return a QName with the appropriate namespace set
     */
    public static QName getQName(String text, String defaultNamespace, NamespaceContext context) {
        if (text == null || text.length() == 0) {
            return null;
        }

        int index = text.indexOf(':');
        if (index < 1 || index == text.length() - 1) {
            // unqualified form - use the default supplied
            return new QName(defaultNamespace, text);
        } else {
            String prefix = text.substring(0, index);
            String uri = context.getNamespaceURI(prefix);
            String localPart = text.substring(index + 1);
            return new QName(uri, localPart, prefix);
        }
    }

    /**
     * Advances to the next tag in the stream according to the semantics of {@link XMLStreamReader#nextTag()} with the additional behavior that
     * characters, spaces, comments, CDATA, and processing instructions will be recorded on the model object.
     *
     * @param type   the model object
     * @param reader the stream
     * @return the next tag value
     * @throws XMLStreamException if a parsing error occurs
     */
    public static int nextTagRecord(ModelObject type, XMLStreamReader reader) throws XMLStreamException {
        int eventType = reader.next();
        while (true) {
            if (eventType == XMLStreamConstants.CHARACTERS) {
                type.addText(reader.getText());
            } else if (eventType == XMLStreamConstants.SPACE) {
                type.addText(reader.getText());
            } else if (eventType == XMLStreamConstants.COMMENT) {
                type.addComment(reader.getText());
            } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION) {
                // FIXME record
            } else if (eventType == XMLStreamConstants.CDATA) {
                // FIXME record
            } else {
                break;
            }
            eventType = reader.next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("Expected start or end tag", reader.getLocation());
        }
        return eventType;
    }

}
