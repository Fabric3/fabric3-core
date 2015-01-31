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
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.databinding.jaxb.factory.JAXBContextFactory;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Node;

/**
 * Creates transformers to convert from a DOM Node to a JAXB object.
 */
public class Node2JAXBTransformerFactory implements TransformerFactory {
    private JAXBContextFactory contextFactory;

    public Node2JAXBTransformerFactory(@Reference JAXBContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public int getOrder() {
        return 0;
    }

    public boolean canTransform(DataType source, DataType target) {
        return Node.class.isAssignableFrom(source.getType()) && target instanceof JavaType;
    }

    public Transformer<?, ?> create(DataType source, DataType target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes) throws ContainerException {
        try {
            Set<Class<?>> types = new HashSet<>(sourceTypes);
            types.addAll(targetTypes);
            JAXBContext jaxbContext = contextFactory.createJAXBContext(types.toArray(new Class<?>[types.size()]));
            if (targetTypes.size() == 1) {
                Class<?> type = targetTypes.iterator().next();
                return createTransformer(source, type, jaxbContext);
            } else if (targetTypes.size() > 1) {
                // the conversion must handle multiple parameters, which will be passed to the transformer in an array
                Transformer<?, ?>[] transformers = new Transformer<?, ?>[sourceTypes.size()];
                for (int i = 0; i < sourceTypes.size(); i++) {
                    Class<?> type = sourceTypes.get(i);
                    transformers[i] = createTransformer(source, type, jaxbContext);
                }
                return new MultiValueArrayTransformer(transformers);
            } else {
                throw new UnsupportedOperationException("Null parameter operations not yet supported");
            }
        } catch (JAXBException e) {
            throw new ContainerException(e);
        }
    }

    private Transformer<Node, Object> createTransformer(DataType source, Class<?> type, JAXBContext jaxbContext) {
        if (type.isAnnotationPresent(XmlRootElement.class)) {
            if (TypeConstants.PROPERTY_TYPE.equals(source)) {
                // the value is a property
                return new PropertyValue2JAXBTransformer(jaxbContext);
            } else {
                return new Node2JAXBTransformer(jaxbContext);
            }
        } else {
            return new Node2JAXBElementTransformer(jaxbContext, type);
        }
    }

}