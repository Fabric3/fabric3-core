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
*/
package org.fabric3.databinding.jaxb.transform;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;

/**
 * Transforms from a JAXB instance not annotated with XmlRootElement to a DOM Document.
 */
public class JAXBElement2DocumentTransformer implements Transformer<Object, Document> {
    private JAXBContext jaxbContext;
    private DocumentBuilderFactory factory;
    private QName name = null;

    public JAXBElement2DocumentTransformer(JAXBContext jaxbContext, QName name) {
        this.jaxbContext = jaxbContext;
        this.name = name;
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    @SuppressWarnings({"unchecked"})
    public Document transform(Object source, ClassLoader loader) throws TransformationException {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Marshaller marshaller = jaxbContext.createMarshaller();

            Class<?> type = source.getClass();
            JAXBElement<?> element = new JAXBElement(name, type, source);
            marshaller.marshal(element, document);

            /*
            Source s1 = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = factory.newTransformer();
            transformer.transform(s1, result);
            System.out.println(stringWriter.getBuffer().toString());    */

            return document;
        } catch (JAXBException e) {
            throw new TransformationException(e);
        } catch (ParserConfigurationException e) {
            throw new TransformationException(e);
        }
    }


}