/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.provision;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.type.java.ManagementInfo;

/**
 * Definition of a physical component whose actual implementation is based on a POJO.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = 297672484973345029L;

    private InstanceFactoryDefinition providerDefinition;
    private String scope;
    private boolean eager;
    private boolean managed;
    private ManagementInfo managementInfo;

    /**
     * Gets the instance factory provider definition.
     *
     * @return Instance factory provider definition.
     */
    public InstanceFactoryDefinition getFactoryDefinition() {
        return providerDefinition;
    }

    /**
     * Sets the instance factory provider definition.
     *
     * @param providerDefinition Instance factory provider definition.
     */
    public void setProviderDefinition(InstanceFactoryDefinition providerDefinition) {
        this.providerDefinition = providerDefinition;
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
