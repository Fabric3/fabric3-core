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
package org.fabric3.api.model.type.resource.application;

import java.util.function.Supplier;

import org.fabric3.api.model.type.component.Resource;

/**
 * Defines a resource provided by a user application.
 */
public class ApplicationResource extends Resource {
    private String name;
    private Supplier<?> supplier;

    /**
     * Constructor.
     *
     * @param name     the resource name, used to resolve it
     * @param supplier the resource factory. The supplier may be invoked multiple times, returning the same instance if the resource should not be recreated on
     *                 each invocation.
     */
    public ApplicationResource(String name, Supplier<?> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public Supplier<?> getSupplier() {
        return supplier;
    }
}
