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
package org.fabric3.api.model.type.component;

import java.io.Serializable;

/**
 * Defines the component implementation instance lifecycle.
 */
public class Scope implements Serializable {
    private static final long serialVersionUID = -5300929173662672089L;
    public static final Scope STATELESS = new Scope("STATELESS", false);
    public static final Scope COMPOSITE = new Scope("COMPOSITE", true);
    public static final Scope DOMAIN = new Scope("DOMAIN", true);

    private final String scope;
    private final boolean singleton;

    public Scope(String scope, boolean singleton) {
        this.singleton = singleton;
        this.scope = scope.toUpperCase().intern();
    }

    public static Scope getScope(String name) {
        if (STATELESS.getScope().equals(name)) {
            return STATELESS;
        } else if (COMPOSITE.getScope().equals(name)) {
            return COMPOSITE;
        } else if (DOMAIN.getScope().equals(name)) {
            return DOMAIN;
        }
        throw new IllegalArgumentException("Unknown scope: " + name);
    }

    public String getScope() {
        return scope;
    }

    public boolean isSingleton() {
        return singleton;
    }

    @SuppressWarnings({"StringEquality"})
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Scope scope1 = (Scope) o;
        return scope == scope1.scope;
    }

    public int hashCode() {
        return scope.hashCode();
    }

    public String toString() {
        return scope;
    }
}
