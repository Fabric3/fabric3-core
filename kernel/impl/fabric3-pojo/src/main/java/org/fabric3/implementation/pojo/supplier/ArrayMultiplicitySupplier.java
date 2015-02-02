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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.supplier;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Returns an <code>Array</code> of object instances.
 */
public class ArrayMultiplicitySupplier extends AbstractCollectionMultiplicitySupplier<List<Supplier<?>>> {
    private Class interfaceType;

    public ArrayMultiplicitySupplier(Class interfaceType) {
        this.interfaceType = interfaceType;
    }

    public Object get() throws Fabric3Exception {
        Object array = Array.newInstance(interfaceType, suppliers.size());
        for (int i = 0; i < suppliers.size(); i++) {
            Array.set(array, i, suppliers.get(i).get());
        }
        return array;
    }

}
