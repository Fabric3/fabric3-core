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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.model.physical.ParamTypes;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.fabric3.spi.model.type.TypeConstants.PROPERTY_TYPE;

/**
 *
 */
public class ArrayBuilderImpl extends AbstractPropertyBuilder implements ArrayBuilder {

    public ArrayBuilderImpl(@Reference TransformerRegistry registry) {
        super(registry);
    }

    @SuppressWarnings({"unchecked"})
    public ObjectFactory<?> createFactory(String name, DataType dataType, Document value, ClassLoader classLoader) throws Fabric3Exception {
        Class componentType = dataType.getType().getComponentType();
        Class<?> type = componentType;
        if (type.isPrimitive()) {
            type = ParamTypes.PRIMITIVE_TO_OBJECT.get(type);
        }

        List<Class<?>> types = new ArrayList<>();
        types.add(type);

        Transformer<Node, ?> transformer = getTransformer(name, PROPERTY_TYPE, new JavaType(type), types);

        Element root = value.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        Object array = Array.newInstance(componentType, nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i).getFirstChild();
            Object val = transformer.transform(node, classLoader);
            Array.set(array, i, val);
        }
        return new SingletonObjectFactory<>(array);
    }

}