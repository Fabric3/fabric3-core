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

import javax.servlet.http.HttpSession;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.pojo.supplier.MultiplicitySupplier;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.injection.Injector;

/**
 * Injects an instance (e.g. a reference proxy) into an HTTP session object.
 */
public class HttpSessionInjector implements Injector<HttpSession> {
    private Supplier<?> supplier;
    private String name;

    public void inject(HttpSession session) throws Fabric3Exception {
        session.setAttribute(name, supplier.get());
    }

    public void setSupplier(Supplier<?> supplier, InjectionAttributes attributes) {
        this.supplier = supplier;
        this.name = attributes.getKey().toString();
    }

    public void clearSupplier() {
        if (this.supplier instanceof MultiplicitySupplier<?>) {
            ((MultiplicitySupplier<?>) this.supplier).clear();
        } else {
            supplier = null;
        }
    }

}
