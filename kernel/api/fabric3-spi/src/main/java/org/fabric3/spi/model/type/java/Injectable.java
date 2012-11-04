/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.spi.model.type.java;

import org.fabric3.model.type.ModelObject;

/**
 * Identifies an attribute of the component that can be injected into an instance.
 */
public class Injectable extends ModelObject {
    private static final long serialVersionUID = -3313258224983902890L;
    public static final Injectable OASIS_COMPONENT_CONTEXT = new Injectable(InjectableType.CONTEXT, "OASISComponentContext");
    public static final Injectable OASIS_REQUEST_CONTEXT = new Injectable(InjectableType.CONTEXT, "RequestContext");

    private InjectableType type;

    private String name;

    /**
     * Constructor used for deserialization.
     */
    public Injectable() {
    }

    /**
     * Constructor specifying type of value and logical name.
     *
     * @param type the type of value
     * @param name the logical name
     */
    public Injectable(InjectableType type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Returns the type (service, reference, property).
     *
     * @return the type of value this source represents
     */
    public InjectableType getType() {
        return type;
    }

    /**
     * Sets the type (callback, reference, property).
     *
     * @param type the type of value this source represents
     */
    public void setType(InjectableType type) {
        this.type = type;
    }

    /**
     * Returns the name.
     *
     * @return the name of this value
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this value.
     *
     * @param name the name of this value
     */
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name + '[' + type + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Injectable that = (Injectable) o;
        return name.equals(that.name) && type == that.type;

    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + name.hashCode();
    }
}
