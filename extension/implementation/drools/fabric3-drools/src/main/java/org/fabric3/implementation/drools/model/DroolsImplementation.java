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
*/
package org.fabric3.implementation.drools.model;

import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Implementation;

/**
 * A Drools component implementation type.
 *
 * @version $$Rev$$ $$Date$$
 */
public class DroolsImplementation extends Implementation<ComponentType> {
    private static final long serialVersionUID = -9156045948000857018L;
    public static final QName IMPLEMENTATION_DROOLS = new QName(Namespaces.F3, "implementation.drools");

    private List<String> resources;

    /**
     * Constructor.
     *
     * @param componentType the component type
     * @param resources     the resources used to construct the Drools knowledge base for this component
     */
    public DroolsImplementation(ComponentType componentType, List<String> resources) {
        super(componentType);
        this.resources = resources;
    }

    public QName getType() {
        return IMPLEMENTATION_DROOLS;
    }

    public List<String> getResources() {
        return resources;
    }
}


