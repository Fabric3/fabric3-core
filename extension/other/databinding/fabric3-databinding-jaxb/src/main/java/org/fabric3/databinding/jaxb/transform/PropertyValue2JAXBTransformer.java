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

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.transform.Transformer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Converts from a component property represented as a DOM Node to a JAXB type. The DOM node representation contains a root &lt;value&gt; element.
 */
public class PropertyValue2JAXBTransformer implements Transformer<Node, Object> {
    private JAXBContext jaxbContext;

    public PropertyValue2JAXBTransformer(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public Object transform(Node source, ClassLoader loader) throws ContainerException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            if ("value".equals(source.getNodeName()) || "key".equals(source.getNodeName())) {
                NodeList children = source.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof Element) {
                        return jaxbContext.createUnmarshaller().unmarshal(children.item(i));
                    }
                }
                throw new ContainerException("Unexpected content");

            } else {
                // global element
                return jaxbContext.createUnmarshaller().unmarshal(source);
            }

        } catch (JAXBException e) {
            throw new ContainerException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

}
