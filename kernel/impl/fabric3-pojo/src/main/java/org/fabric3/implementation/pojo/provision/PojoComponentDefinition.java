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
package org.fabric3.implementation.pojo.provision;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.api.model.type.java.ManagementInfo;

/**
 * Definition of a physical component whose actual implementation is based on a POJO.
 */
public abstract class PojoComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = 297672484973345029L;

    private ImplementationManagerDefinition managerDefinition;
    private String scope;
    private boolean eager;
    private boolean managed;
    private ManagementInfo managementInfo;

    /**
     * Gets the instance factory provider definition.
     *
     * @return Instance factory provider definition.
     */
    public ImplementationManagerDefinition getFactoryDefinition() {
        return managerDefinition;
    }

    /**
     * Sets the instance factory provider definition.
     *
     * @param managerDefinition Instance factory provider definition.
     */
    public void setManagerDefinition(ImplementationManagerDefinition managerDefinition) {
        this.managerDefinition = managerDefinition;
    }

    /**
     * Gets the component scope.
     *
     * @return the component scope.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the component scope.
     *
     * @param scope the component scope.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }


    /**
     * Returns true if the component should be eager initialized.
     *
     * @return true if the component should be eager initialized
     */
    public boolean isEagerInit() {
        return eager;
    }

    /**
     * Sets if the component should be eager initialized.
     *
     * @param eager true if the component should be eager initialized
     */
    public void setEagerInit(boolean eager) {
        this.eager = eager;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    public ManagementInfo getManagementInfo() {
        return managementInfo;
    }

    public void setManagementInfo(ManagementInfo managementInfo) {
        this.managementInfo = managementInfo;
    }
}
