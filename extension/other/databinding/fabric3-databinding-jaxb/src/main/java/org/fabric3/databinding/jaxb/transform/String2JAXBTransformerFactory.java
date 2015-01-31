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

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.databinding.jaxb.factory.JAXBContextFactory;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates Transformers capable of marshalling serialized XML Strings to JAXB types.
 */
public class String2JAXBTransformerFactory implements TransformerFactory {
    private JAXBContextFactory contextFactory;

    public String2JAXBTransformerFactory(@Reference JAXBContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public int getOrder() {
        return 0;
    }

    public boolean canTransform(DataType source, DataType target) {
        return "JAXB".equals(source.getDatabinding()) && String.class.equals(source.getType()) && target instanceof JavaType;
    }

    public Transformer<?, ?> create(DataType source, DataType target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes) throws ContainerException {
        try {
            Set<Class<?>> types = new HashSet<>(sourceTypes);
            types.addAll(targetTypes);
            JAXBContext jaxbContext = contextFactory.createJAXBContext(types.toArray(new Class<?>[types.size()]));
            if (sourceTypes.size() == 1) {
                Class<?> type = targetTypes.iterator().next();
                return createTransformer(type, jaxbContext);
            } else if (sourceTypes.size() > 1) {
                // the conversion must handle multiple parameters, which will be passed to the transformer in an array
                Transformer<?, ?>[] transformers = new Transformer<?, ?>[sourceTypes.size()];
                for (int i = 0; i < sourceTypes.size(); i++) {
                    Class<?> type = sourceTypes.get(i);
                    transformers[i] = createTransformer(type, jaxbContext);
                }
                return new MultiValueArrayTransformer(transformers);
            } else {
                return createTransformer(target.getType(), jaxbContext);
            }
        } catch (JAXBException e) {
            throw new ContainerException(e);
        }
    }

    private Transformer<String, Object> createTransformer(Class<?> type, JAXBContext jaxbContext) {
        if (type.isAnnotationPresent(XmlRootElement.class)) {
            return new String2JAXBObjectTransformer(jaxbContext);
        } else {
            return new String2JAXBElementTransformer(jaxbContext, type);
        }
    }

}