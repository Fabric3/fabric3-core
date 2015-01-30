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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.transform.Transformer;
import org.w3c.dom.Document;

/**
 * Transforms from a JAXB top-level element instance to a DOM Document.
 */
public class JAXBObject2DocumentTransformer implements Transformer<Object, Document> {
    private JAXBContext jaxbContext;
    private DocumentBuilderFactory factory;

    public JAXBObject2DocumentTransformer(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    public Document transform(Object source, ClassLoader loader) throws ContainerException {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(source, document);
            return document;
        } catch (JAXBException | ParserConfigurationException e) {
            throw new ContainerException(e);
        }
    }


}
