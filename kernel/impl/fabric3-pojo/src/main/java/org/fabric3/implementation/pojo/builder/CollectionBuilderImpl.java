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
import java.util.Collection;
import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
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
public class CollectionBuilderImpl extends AbstractPropertyBuilder implements CollectionBuilder {

    public CollectionBuilderImpl(@Reference TransformerRegistry registry) {
        super(registry);
    }

    @SuppressWarnings({"unchecked"})
    public <T> ObjectFactory<Collection<T>> createFactory(Collection<T> collection,
                                                          String name,
                                                          JavaGenericType dataType,
                                                          Document value,
                                                          ClassLoader classLoader) throws ContainerException {
        List<JavaTypeInfo> typeInfos = dataType.getTypeInfo().getParameterTypesInfos();
        if (typeInfos.size() < 1) {
            // programming error
            throw new ContainerException("Collection properties must have a value type");
        }
        Class<?> type = typeInfos.get(0).getRawType();

        List<Class<?>> types = new ArrayList<>();
        types.add(type);

        Transformer<Node, ?> transformer = getTransformer(name, PROPERTY_TYPE, new JavaType(type), types);

        Element root = value.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i).getFirstChild();
            T val = (T) transformer.transform(node, classLoader);
            collection.add(val);
        }
        return new SingletonObjectFactory<>(collection);
    }

}