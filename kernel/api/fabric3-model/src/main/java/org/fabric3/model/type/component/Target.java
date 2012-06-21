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
*/
package org.fabric3.model.type.component;

import org.fabric3.model.type.ModelObject;

/**
 * Encapsulates information needed to identify a component/service, component/service/binding, component/reference or component/reference/binding.
 * Targets are relative to a composite and not absolute.
 *
 * @version $Rev$ $Date$
 */
public class Target extends ModelObject {
    private static final long serialVersionUID = 8616545726099554138L;

    private String component;
    private String bindable;
    private String binding;

    public Target(String component) {
        this.component = component;
    }

    public Target(String component, String bindable) {
        this.component = component;
        this.bindable = bindable;
    }

    public Target(String component, String bindable, String binding) {
        this.component = component;
        this.bindable = bindable;
        this.binding = binding;
    }

    /**
     * Returns the component name.
     *
     * @return the component name
     */
    public String getComponent() {
        return component;
    }

    /**
     * Returns the reference or service name.
     *
     * @return the reference or service name. May be null.
     */
    public String getBindable() {
        return bindable;
    }

    /**
     * Returns the binding name.
     *
     * @return the binding name. May be null.
     */
    public String getBinding() {
        return binding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        return !(bindable != null ? !bindable.equals(target.bindable) : target.bindable != null)
                && !(binding != null ? !binding.equals(target.binding) : target.binding != null)
                && !(component != null ? !component.equals(target.component) : target.component != null);

    }

    @Override
    public int hashCode() {
        int result = component != null ? component.hashCode() : 0;
        result = 31 * result + (bindable != null ? bindable.hashCode() : 0);
        result = 31 * result + (binding != null ? binding.hashCode() : 0);
        return result;
    }
}
