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
*/
package org.fabric3.fabric.deployment.instantiator;

import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Instantiates explicit wires, i.e. those declared by a <code>wire<code> wire element or using the <code>target</code> attribute on a
 * <code>reference</code> element. During instantiation, wires will be validated.
 * <p/>
 * Note {@link #instantiateCompositeWires(Composite, LogicalCompositeComponent, InstantiationContext)} must be called before {@link
 * #instantiateReferenceWires(LogicalComponent, InstantiationContext)} as composite <code>&lt;wire&gt;</code> elements may use the @replace attribute
 * to overwrite wires created using the @target attribute on a <code>&lt;reference&gt;</code> element. If composite wires with replace set to true
 * exist, wires base on the reference target attribute will not be created. 
 */
public interface WireInstantiator {

    /**
     * Instantiates wires declared using a <code>wire</code> element in a composite.
     *
     * @param composite the composite
     * @param parent    the logical composite where the wires will be added
     * @param context   the instantiation context.
     */
    void instantiateCompositeWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context);

    /**
     * Instantiates wires declared using the <code>target</code> attribute on a <code>reference</code> element.
     *
     * @param component the logical component containing the configured references
     * @param context   the instantiation context.
     */
    void instantiateReferenceWires(LogicalComponent<?> component, InstantiationContext context);
}
