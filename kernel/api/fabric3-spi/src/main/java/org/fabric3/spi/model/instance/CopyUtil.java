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
*/
package org.fabric3.spi.model.instance;

import java.net.URI;
import javax.xml.namespace.QName;

/**
 * Utilities for copying a logical model graph.
 *
 * @version $Rev$ $Date$
 */
public class CopyUtil {
    private CopyUtil() {
    }

    /**
     * Copies the instance graph, making a complete replica, including preservation of parent-child relationships.
     *
     * @param composite the composite to copy
     * @return the copy
     */
    public static LogicalCompositeComponent copy(LogicalCompositeComponent composite) {
        return copy(composite, composite.getParent());
    }


    /**
     * Recursively performs the actual copy.
     *
     * @param composite the composite to copy
     * @param parent    the parent of the copy
     * @return the copy
     */
    private static LogicalCompositeComponent copy(LogicalCompositeComponent composite, LogicalCompositeComponent parent) {
        LogicalCompositeComponent copy =
                new LogicalCompositeComponent(composite.getUri(), composite.getDefinition(), parent);
        copy.setAutowire(composite.getAutowire());
        copy.setState(composite.getState());
        copy.setZone(composite.getZone());
        copy.addIntents(composite.getIntents());
        copy.addPolicySets(composite.getPolicySets());
        for (LogicalProperty property : composite.getAllProperties().values()) {
            copy.setProperties(property);
        }
        for (LogicalComponent<?> component : composite.getComponents()) {
            copy(component, copy);
        }
        for (LogicalReference reference : composite.getReferences()) {
            copy(reference, copy);
        }
        for (LogicalResource<?> resource : composite.getResources()) {
            copy(resource, copy);
        }
        for (LogicalService service : composite.getServices()) {
            copy(service, copy);
        }
        for (LogicalReference reference : copy.getReferences()) {
            copyWires(composite, reference, copy);
        }
        return copy;
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalComponent<?> component, LogicalCompositeComponent parent) {
        LogicalComponent<?> copy;
        if (component instanceof LogicalCompositeComponent) {
            copy = copy((LogicalCompositeComponent) component, parent);
        } else {
            copy = new LogicalComponent(component.getUri(), component.getDefinition(), parent);
            copy.setZone(component.getZone());
            copy.addIntents(component.getIntents());
            copy.addPolicySets(component.getPolicySets());
            copy.setState(component.getState());
        }
        parent.addComponent(copy);
    }

    private static void copy(LogicalReference reference, LogicalCompositeComponent parent) {
        LogicalReference copy = new LogicalReference(reference.getUri(), reference.getDefinition(), parent);
        for (URI uri : reference.getPromotedUris()) {
            copy.addPromotedUri(uri);
        }
        copy.addIntents(reference.getIntents());
        copy.addPolicySets(reference.getPolicySets());
        copy(reference, copy);
        parent.addReference(copy);
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalResource<?> resource, LogicalCompositeComponent parent) {
        LogicalResource copy = new LogicalResource(resource.getUri(), resource.getResourceDefinition(), parent);
        copy.setTarget(resource.getTarget());
        parent.addResource(copy);
    }


    private static void copy(LogicalService service, LogicalCompositeComponent parent) {
        LogicalService copy = new LogicalService(service.getUri(), service.getDefinition(), parent);
        copy.setPromotedUri(service.getPromotedUri());
        copy(service, copy);
        copy.addIntents(service.getIntents());
        copy.addPolicySets(service.getPolicySets());
        parent.addService(copy);
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(Bindable from, Bindable to) {
        for (LogicalBinding<?> binding : from.getBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to);
            copy.setState(binding.getState());
            to.addBinding(copy);
            copy.addIntents(binding.getIntents());
            copy.addPolicySets(binding.getPolicySets());
        }
        for (LogicalBinding<?> binding : from.getCallbackBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to);
            copy.setState(binding.getState());
            to.addCallbackBinding(copy);
            copy.setAssigned(binding.isAssigned());
            copy.addIntents(binding.getIntents());
            copy.addPolicySets(binding.getPolicySets());
        }
    }

    private static void copyWires(LogicalCompositeComponent composite, LogicalReference reference, LogicalCompositeComponent parent) {
        for (LogicalWire wire : composite.getWires(reference)) {
            QName deployable = wire.getTargetDeployable();
            LogicalService target = wire.getTarget();
            LogicalWire wireCopy = new LogicalWire(parent, reference, target, deployable);
            wireCopy.setSourceBinding(wire.getSourceBinding());
            wireCopy.setTargetBinding(wire.getTargetBinding());
            parent.addWire(reference, wireCopy);
        }
    }

}
