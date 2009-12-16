/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.model.type.component;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.AbstractPolicyAware;
import org.fabric3.model.type.CapabilityAware;

/**
 * Represents a component implementation.
 *
 * @version $Rev$ $Date$
 */
public abstract class Implementation<T extends AbstractComponentType<?, ?, ?, ?>> extends AbstractPolicyAware implements CapabilityAware {
    private static final long serialVersionUID = -6060603636927660850L;
    private T componentType;
    private final Set<String> requiredCapabilities = new HashSet<String>();

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
        this.componentType = componentType;
    }

    /**
     * Returns true if this implementation corresponds to the supplied XML element.
     *
     * @param type the QName of the implementation element
     * @return true if this instance is of the supplied type
     */
    public boolean isType(QName type) {
        return getType().equals(type);
    }

    /**
     * Returns the runtime capabilities required to host this type.
     *
     * @return the runtime capabilities required to host this type
     */
    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    /**
     * Sets the runtime capabilities required to host this type.
     *
     * @param capability the runtime capabilities required to host this type
     */
    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }

    /**
     * Returns the XML element corresponding to this type.
     *
     * @return the XML element corresponding to this type
     */
    public abstract QName getType();


}
