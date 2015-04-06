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
package org.fabric3.node.nonmanaged;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.util.Closeable;

/**
 *
 */
public class NonManagedConnectionTarget extends PhysicalConnectionTarget {
    private transient Object proxy;
    private Closeable closeable;

    public NonManagedConnectionTarget(URI uri, Class<?> interfaze) {
        setUri(uri);
        setServiceInterface(interfaze);
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public Closeable getCloseable() {
        return closeable;
    }

    public void setCloseable(Closeable closeable) {
        this.closeable = closeable;
    }
}
