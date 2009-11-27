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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Service interface for loading XML documents from a file as DOM objects.
 *
 * @version $Rev$ $Date$
 */
public interface DocumentLoader {
    /**
     * Loads a Document from a local file.
     *
     * @param file the file containing the XML document
     * @return the content of the file as a Document
     * @throws IOException  if there was a problem reading the file
     * @throws SAXException if there was a problem with the document
     */
    Document load(File file) throws IOException, SAXException;

    /**
     * Loads a Document from a physical resource.
     *
     * @param url the location of the resource
     * @return the content of the resource as a Document
     * @throws IOException  if there was a problem reading the resource
     * @throws SAXException if there was a problem with the document
     */
    Document load(URL url) throws IOException, SAXException;

    /**
     * Loads a Document from a logical resource.
     * <p/>
     * How the resource is converted to a physical location is implementation defined.
     *
     * @param uri the logical location of the resource
     * @return the content of the resource as a Document
     * @throws IOException  if there was a problem reading the resource
     * @throws SAXException if there was a problem with the document
     */
    Document load(URI uri) throws IOException, SAXException;

    /**
     * Loads a Document from a logical source.
     *
     * @param source the source of the document text
     * @return the content as a Document
     * @throws IOException  if there was a problem reading the content
     * @throws SAXException if there was a problem with the document
     */
    Document load(InputSource source) throws IOException, SAXException;
}
