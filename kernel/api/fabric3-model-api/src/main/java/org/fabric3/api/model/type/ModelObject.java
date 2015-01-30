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
package org.fabric3.api.model.type;

import java.io.Serializable;

/**
 * The base class for model types.
 */
public abstract class ModelObject<P extends ModelObject> implements Serializable {
    private static final long serialVersionUID = -4731760911483352681L;

    private P parent;

    /**
     * Returns the parent of this object or null if there is no parent.
     *
     * @return the parent of this object or nul
     */
    public P getParent() {
        return parent;
    }

    /**
     * Sets the parent of this object or null if there is no parent.
     *
     * @param parent the parent of this object or nul
     */
    public void setParent(P parent) {
        this.parent = parent;
    }
}
