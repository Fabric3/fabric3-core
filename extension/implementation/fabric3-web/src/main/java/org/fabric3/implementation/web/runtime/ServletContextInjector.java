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
package org.fabric3.implementation.web.runtime;

import javax.servlet.ServletContext;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.supplier.MultiplicitySupplier;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.injection.Injector;

/**
 * Injects objects (reference proxies, properties, contexts) into a ServletContext.
 */
public class ServletContextInjector implements Injector<ServletContext> {
    private Supplier<?> supplier;
    private String key;

    public void inject(ServletContext context) throws Fabric3Exception {
        context.setAttribute(key, supplier.get());
    }

    public void setSupplier(Supplier<?> supplier, InjectionAttributes attributes) {
        this.supplier = supplier;
        this.key = attributes.getKey().toString();
    }

    public void clearSupplier() {
        if (this.supplier instanceof MultiplicitySupplier<?>) {
            ((MultiplicitySupplier<?>) this.supplier).clear();
        } else {
            supplier = null;
            key = null;
        }
    }

}
