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
package org.fabric3.jpa.provision;

/**
 * Contains attach point metadata for an EntityManager and Hibernate Session resource.
 */
public abstract class AbstractContextWireTargetDefinition extends AbstractWireTargetDefinition {
    private static final long serialVersionUID = -6823873953780670817L;
    private boolean extended;
    private boolean multiThreaded = true;

    protected AbstractContextWireTargetDefinition(String unitName) {
        super(unitName);
    }

    /**
     * Returns true if the persistence unit is accessed from a multi-threaded (e.g. composite-scoped) component.
     *
     * @return true if the persistence unit is accessed from a multi-threaded (e.g. composite-scoped) component.
     */
    public boolean isMultiThreaded() {
        return multiThreaded;
    }

    /**
     * Sets the threading environment in which the persistence unit is accessed.
     *
     * @param multiThreaded true if the persistence unit is accessed from a multi-threaded (e.g. composite-scoped) component.
     */
    public void setMultiThreaded(boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
    }


}