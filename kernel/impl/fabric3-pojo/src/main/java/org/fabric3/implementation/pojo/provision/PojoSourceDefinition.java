/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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

import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.api.model.type.java.Injectable;

/**
 *
 */
public class PojoSourceDefinition extends PhysicalSourceDefinition {
    private static final long serialVersionUID = -7594088400247150995L;
    private String interfaceName;
    private Injectable injectable;
    private boolean keyed;
    private String keyClassName;

    /**
     * Returns the name of the Java interface for the service contract.
     *
     * @return the name of the Java interface for the service contract
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets the name of the Java interface for the service contract.
     *
     * @param interfaceName the name of the Java interface for the service contract
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Returns the Injectable this wire applies to.
     *
     * @return the the injectable this wire applies to
     */
    public Injectable getInjectable() {
        return injectable;
    }

    /**
     * Sets the Injectable for this wire applies to.
     *
     * @param injectable the injectable this wire applies to
     */
    public void setInjectable(Injectable injectable) {
        this.injectable = injectable;
    }

    /**
     * Returns true if the reference is a keyed reference, i.e. is a map-style multiplicity.
     *
     * @return true if the reference is a keyed reference
     */
    public boolean isKeyed() {
        return keyed;
    }

    /**
     * Sets if if the reference is a keyed reference.
     *
     * @param keyed true if the reference is a keyed reference
     */
    public void setKeyed(boolean keyed) {
        this.keyed = keyed;
    }

    /**
     * Returns the reference key class name.
     *
     * @return the reference key class name.
     */
    public String getKeyClassName() {
        return keyClassName;
    }

    /**
     * Sets the reference key class name.
     *
     * @param name the reference key class name.
     */
    public void setKeyClassName(String name) {
        this.keyClassName = name;
    }
}
