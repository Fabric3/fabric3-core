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
import org.w3c.dom.Node;

/**
 * Converts from a DOM Node to a JAXB type serialized as a JAXBElement.
 */
public class Node2JAXBElementTransformer implements Transformer<Node, Object> {
    private JAXBContext jaxbContext;
    private Class<?> declaredType;

    public Node2JAXBElementTransformer(JAXBContext jaxbContext, Class<?> declaredType) {
        this.jaxbContext = jaxbContext;
        this.declaredType = declaredType;
    }

    public Object transform(Node source, ClassLoader loader) throws ContainerException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            return jaxbContext.createUnmarshaller().unmarshal(source, declaredType).getValue();
        } catch (JAXBException e) {
            throw new ContainerException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

}