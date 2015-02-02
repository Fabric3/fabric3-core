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
package org.fabric3.api.model.type.java;

import org.fabric3.api.model.type.ModelObject;

/**
 * Represents an injection site on a Java-based component implementation.
 */
public class InjectionSite extends ModelObject<InjectingComponentType> {

    // Name of type being injected
    private String type;

    protected InjectionSite(String type) {
        this.type = type;
    }

    protected InjectionSite() {
        // required for deserialization
    }

    /**
     * Returns the Java type being injected, i.e. the class name.
     *
     * @return the name of the Java type being injected
     */
    public String getType() {
        return type;
    }
}
