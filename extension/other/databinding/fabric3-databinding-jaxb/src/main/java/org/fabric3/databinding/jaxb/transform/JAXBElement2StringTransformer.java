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
import java.io.StringWriter;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.transform.Transformer;

/**
 * Transforms from a JAXB instance not annotated with XmlRootElement to a serialized String.
 */
public class JAXBElement2StringTransformer implements Transformer<Object, String> {
    private JAXBContext jaxbContext;
    private QName name = null;

    public JAXBElement2StringTransformer(JAXBContext jaxbContext, QName name) {
        this.jaxbContext = jaxbContext;
        this.name = name;
    }

    @SuppressWarnings({"unchecked"})
    public String transform(Object source, ClassLoader loader) throws ContainerException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            Class<?> type = source.getClass();
            JAXBElement<?> element = new JAXBElement(name, type, source);
            StringWriter writer = new StringWriter();
            marshaller.marshal(element, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new ContainerException(e);
        }
    }

}