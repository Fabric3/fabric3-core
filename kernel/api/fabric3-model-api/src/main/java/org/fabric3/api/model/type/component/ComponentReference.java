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
package org.fabric3.api.model.type.component;

/**
 * A reference configured on a component.
 */
public class ComponentReference extends AbstractReference<ComponentDefinition> {
    private static final long serialVersionUID = 2072898078368317712L;

    private boolean nonOverridable;
    private ComponentDefinition parent;

    /**
     * Constructor.
     *
     * @param name         the name of the reference being configured
     * @param multiplicity the reference multiplicity
     */
    public ComponentReference(String name, Multiplicity multiplicity) {
        super(name, null, multiplicity);
    }

    /**
     * Returns the parent component of this reference.
     *
     * @return the parent component
     */
    public ComponentDefinition getComponent() {
        return parent;
    }

    /**
     * Sets the parent component of this reference.
     *
     * @param parent the parent component
     */
    public void setParent(ComponentDefinition parent) {
        this.parent = parent;
    }

    public boolean isNonOverridable() {
        return nonOverridable;
    }

    public void setNonOverridable(boolean nonOverridable) {
        this.nonOverridable = nonOverridable;
    }
}
