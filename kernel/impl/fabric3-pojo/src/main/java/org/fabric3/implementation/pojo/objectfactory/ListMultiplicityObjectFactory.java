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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.objectfactory;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Returns a <code>List</code> of object instances.
 */
public class ListMultiplicityObjectFactory extends AbstractCollectionMultiplicityObjectFactory<List<ObjectFactory<?>>> {

    public List<Object> getInstance() throws Fabric3Exception {
        List<Object> list = new ArrayList<>();
        for (ObjectFactory<?> factory : factories) {
            list.add(factory.getInstance());
        }
        return list;
    }
}
