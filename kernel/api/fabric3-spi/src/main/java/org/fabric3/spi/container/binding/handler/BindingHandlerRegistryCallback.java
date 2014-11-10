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
package org.fabric3.spi.container.binding.handler;

import java.util.List;
import javax.xml.namespace.QName;

/**
 * Implemented by a binding extension to receive callbacks when a {@link BindingHandler} becomes available or is removed.
 */
public interface BindingHandlerRegistryCallback<T> {

    /**
     * The fully qualified binding name corresponding to the SCA architected binding name scheme binding.xxxx.
     *
     * @return the fully qualified binding name
     */
    QName getType();

    /**
     * Called when the list of handlers for the binding changes, e.g. one was added or removed. Implementations may not modify the list.
     *
     * @param handlers the new list of handlers
     */
    void update(List<BindingHandler<T>> handlers);

}
