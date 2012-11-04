/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.contribution.processor;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Wraps an XMLStreamReader to override the Location system id.
 */
public class LocationAwareXMLStreamReader implements XMLStreamReader {
    private XMLStreamReader delegate;
    private Location location;

    public LocationAwareXMLStreamReader(XMLStreamReader delegate, String systemId) {
        this.delegate = delegate;
        this.location = new LocationWrapper(delegate.getLocation(), systemId);
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return delegate.getProperty(name);
    }

    public int next() throws XMLStreamException {
        return delegate.next();
    }

    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        delegate.require(type, namespaceURI, localName);
    }

    public String getElementText() throws XMLStreamException {
        return delegate.getElementText();
    }

    public int nextTag() throws XMLStreamException {
        return delegate.nextTag();
    }

    public boolean hasNext() throws XMLStreamException {
        return delegate.hasNext();
    }

    public void close() throws XMLStreamException {
        delegate.close();
    }

    public String getNamespaceURI(String prefix) {
        return delegate.getNamespaceURI(prefix);
    }

    public boolean isStartElement() {
        return delegate.isStartElement();
    }

    public boolean isEndElement() {
        return delegate.isEndElement();
    }

    public boolean isCharacters() {
        return delegate.isCharacters();
    }

    public boolean isWhiteSpace() {
        return delegate.isWhiteSpace();
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        return delegate.getAttributeValue(namespaceURI, localName);
    }

    public int getAttributeCount() {
        return delegate.getAttributeCount();
    }

    public QName getAttributeName(int index) {
        return delegate.getAttributeName(index);
    }

    public String getAttributeNamespace(int index) {
        return delegate.getAttributeNamespace(index);
    }

    public String getAttributeLocalName(int index) {
        return delegate.getAttributeLocalName(index);
    }

    public String getAttributePrefix(int index) {
        return delegate.getAttributePrefix(index);
    }

    public String getAttributeType(int index) {
        return delegate.getAttributeType(index);
    }

    public String getAttributeValue(int index) {
        return delegate.getAttributeValue(index);
    }

    public boolean isAttributeSpecified(int index) {
        return delegate.isAttributeSpecified(index);
    }

    public int getNamespaceCount() {
        return delegate.getNamespaceCount();
    }

    public String getNamespacePrefix(int index) {
        return delegate.getNamespacePrefix(index);
    }

    public String getNamespaceURI(int index) {
        return delegate.getNamespaceURI(index);
    }

    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    public int getEventType() {
        return delegate.getEventType();
    }

    public String getText() {
        return delegate.getText();
    }

    public char[] getTextCharacters() {
        return delegate.getTextCharacters();
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return delegate.getTextCharacters(sourceStart, target, targetStart, length);
    }

    public int getTextStart() {
        return delegate.getTextStart();
    }

    public int getTextLength() {
        return delegate.getTextLength();
    }

    public String getEncoding() {
        return delegate.getEncoding();
    }

    public boolean hasText() {
        return delegate.hasText();
    }

    public Location getLocation() {
        return location;
    }

    public QName getName() {
        return delegate.getName();
    }

    public String getLocalName() {
        return delegate.getLocalName();
    }

    public boolean hasName() {
        return delegate.hasName();
    }

    public String getNamespaceURI() {
        return delegate.getNamespaceURI();
    }

    public String getPrefix() {
        return delegate.getPrefix();
    }

    public String getVersion() {
        return delegate.getVersion();
    }

    public boolean isStandalone() {
        return delegate.isStandalone();
    }

    public boolean standaloneSet() {
        return delegate.standaloneSet();
    }

    public String getCharacterEncodingScheme() {
        return delegate.getCharacterEncodingScheme();
    }

    public String getPITarget() {
        return delegate.getPITarget();
    }

    public String getPIData() {
        return delegate.getPIData();
    }

}
