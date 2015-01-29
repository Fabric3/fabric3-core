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

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.ModelObject;

/**
 * A base component implementation.
 */
public abstract class Implementation<T extends ComponentType> extends ModelObject<ComponentDefinition> {
    private static final long serialVersionUID = -6060603636927660850L;

    private T componentType;

    protected Implementation() {
    }

    protected Implementation(T componentType) {
        this.componentType = componentType;
    }

    /**
     * Returns the component type.
     *
     * @return the component type
     */
    public T getComponentType() {
        return componentType;
    }

    /**
     * Sets the component type.
     *
     * @param componentType the component type
     */
    public void setComponentType(T componentType) {
        componentType.setParent(this);
        this.componentType = componentType;
    }

    /**
     * Returns the implementation artifact name.
     *
     * @return the implementation artifact name
     */
    public String getArtifactName() {
        return null;
    }

    /**
     * Returns the XML element corresponding to this type.
     *
     * @return the XML element corresponding to this type
     */
    public abstract QName getType();

}
