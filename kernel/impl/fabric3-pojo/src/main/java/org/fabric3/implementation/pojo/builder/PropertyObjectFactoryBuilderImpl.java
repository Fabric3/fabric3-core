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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;

/**
 *
 */
public class PropertyObjectFactoryBuilderImpl implements PropertyObjectFactoryBuilder {
    private ArrayBuilder arrayBuilder;
    private CollectionBuilder collectionBuilder;
    private MapBuilder mapBuilder;
    private ObjectBuilder objectBuilder;

    public PropertyObjectFactoryBuilderImpl(@Reference ArrayBuilder arrayBuilder,
                                            @Reference CollectionBuilder collectionBuilder,
                                            @Reference MapBuilder mapBuilder,
                                            @Reference ObjectBuilder objectBuilder) {
        this.arrayBuilder = arrayBuilder;
        this.collectionBuilder = collectionBuilder;
        this.mapBuilder = mapBuilder;
        this.objectBuilder = objectBuilder;
    }

    public ObjectFactory<?> createFactory(String name, DataType dataType, Document value, boolean many, ClassLoader classLoader)
            throws ContainerException {
        Class<?> type = dataType.getType();
        if (type.isArray()) {
            return arrayBuilder.createFactory(name, dataType, value, classLoader);
        } else if (Map.class.equals(type)) {
            return mapBuilder.createFactory(name, (JavaGenericType) dataType, value, classLoader);
        } else if (List.class.equals(type)) {
            return collectionBuilder.createFactory(new ArrayList<>(), name, (JavaGenericType) dataType, value, classLoader);
        } else if (Set.class.equals(type)) {
            return collectionBuilder.createFactory(new HashSet<>(), name, (JavaGenericType) dataType, value, classLoader);
        } else if (LinkedList.class.equals(type)) {
            return collectionBuilder.createFactory(new LinkedList<>(), name, (JavaGenericType) dataType, value, classLoader);
        } else {
            return objectBuilder.createFactory(name, dataType, value, classLoader);
        }
    }
}