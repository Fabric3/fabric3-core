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

package org.fabric3.implementation.pojo.builder;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.fabric3.spi.model.type.TypeConstants.PROPERTY_TYPE;

/**
 *
 */
public class ObjectBuilderImpl extends AbstractPropertyBuilder implements ObjectBuilder {

    public ObjectBuilderImpl(@Reference TransformerRegistry transformerRegistry) {
        super(transformerRegistry);
    }

    public ObjectFactory<?> createFactory(String name, DataType dataType, Document value, ClassLoader classLoader) throws ContainerException {
        Class<?> type = dataType.getType();
        List<Class<?>> types = new ArrayList<>();
        types.add(type);
        Transformer<Node, ?> transformer = getTransformer(name, PROPERTY_TYPE, dataType, types);
        Element element = (Element) value.getDocumentElement().getFirstChild();
        Object instance = transformer.transform(element, classLoader);
        return new SingletonObjectFactory<>(instance);
    }

}