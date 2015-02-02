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
package org.fabric3.spi.container.injection;

/**
 * Attributes of a wire used during injection.
 */
public class InjectionAttributes {
    public static final InjectionAttributes EMPTY_ATTRIBUTES = new InjectionAttributes();
    private Object key;
    private int order = Integer.MIN_VALUE;

    /**
     * Constructor.
     *
     * @param key   the key or Map-based references or null
     * @param order the order for ordered (e.g. List or array)-based references; may be null if not defined or the reference being injected is not
     *              order-sensitive.
     */
    public InjectionAttributes(Object key, int order) {
        this.key = key;
        this.order = order;
    }

    private InjectionAttributes() {
    }

    public Object getKey() {
        return key;
    }

    public int getOrder() {
        return order;
    }
}
