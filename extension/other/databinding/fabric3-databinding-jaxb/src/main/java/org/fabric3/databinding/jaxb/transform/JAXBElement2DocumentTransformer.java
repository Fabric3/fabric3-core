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
        } catch (JAXBException | ParserConfigurationException e) {
            throw new TransformationException(e);
        }
    }


}