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
package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the PromotionNormalizer.
 *
 * @version $Rev$ $Date$
 */
public class PromotionNormalizerImpl implements PromotionNormalizer {

    public void normalize(LogicalComponent<?> component) {
        normalizeServiceBindings(component);
        normalizeReferenceBindingsAndWires(component);
    }

    private void normalizeServiceBindings(LogicalComponent<?> component) {
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        for (LogicalService service : component.getServices()) {
            URI serviceUri = service.getUri();
            List<LogicalBinding<?>> bindings = recurseServicePromotionPath(serviceUri, parent);
            if (bindings.isEmpty()) {
                continue;
            }
            service.overrideBindings(resetParent(bindings, service));
        }
    }

    private List<LogicalBinding<?>> recurseServicePromotionPath(URI serviceUri, LogicalComponent<CompositeImplementation> parent) {
        List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
        for (LogicalService service : parent.getServices()) {
            URI targetUri = service.getPromotedUri();
            if (targetUri.getFragment() == null) {
                // no service specified
                if (targetUri.equals(UriHelper.getDefragmentedName(serviceUri))) {
                    if (parent.getParent() != null) {
                        List<LogicalBinding<?>> list = recurseServicePromotionPath(service.getUri(), parent.getParent());
                        if (list.isEmpty()) {
                            // no bindings were overridden
                            bindings.addAll(service.getBindings());
                        } else {
                            bindings.addAll(list);
                        }
                    } else {
                        bindings.addAll(service.getBindings());
                    }
                }

            } else {
                if (targetUri.equals(serviceUri)) {
                    if (parent.getParent() != null) {
                        List<LogicalBinding<?>> list = recurseServicePromotionPath(service.getUri(), parent.getParent());
                        if (list.isEmpty()) {
                            // no bindings were overridden
                            bindings.addAll(service.getBindings());
                        } else {
                            bindings.addAll(list);
                        }

                    } else {
                        bindings.addAll(service.getBindings());
                    }
                }
            }
        }
        return bindings;
    }

    private void normalizeReferenceBindingsAndWires(LogicalComponent<?> component) {
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        for (LogicalReference reference : component.getReferences()) {
            URI referenceUri = reference.getUri();
            List<LogicalReference> references = recurseReferencePromotionPath(referenceUri, parent);
            if (references.isEmpty()) {
                continue;
            }
            List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
            List<LogicalWire> wiresFromPromotedReferences = new ArrayList<LogicalWire>();
            List<LogicalWire> wires = new ArrayList<LogicalWire>();
            for (LogicalReference promoted : references) {
                bindings.addAll(promoted.getBindings());
                for (LogicalWire logicalWire : promoted.getWires()) {
                    wiresFromPromotedReferences.add(logicalWire);

                }
            }
            if (!bindings.isEmpty()) {
                reference.overrideBindings(resetParent(bindings, reference));
            }
            if (!wiresFromPromotedReferences.isEmpty()) {
                for (LogicalWire promotedWire : wiresFromPromotedReferences) {
                    LogicalService target = promotedWire.getTarget();
                    QName deployable = promotedWire.getParent().getDeployable();
                    LogicalWire wire = new LogicalWire(parent, reference, target, deployable);
                    wires.add(wire);
                }
                ((LogicalCompositeComponent) parent).overrideWires(reference, wires);
            }
        }
    }

    private List<LogicalReference> recurseReferencePromotionPath(URI referenceUri, LogicalComponent<CompositeImplementation> parent) {
        List<LogicalReference> references = new ArrayList<LogicalReference>();
        for (LogicalReference reference : parent.getReferences()) {
            for (URI targetUri : reference.getPromotedUris()) {
                if (targetUri.equals(referenceUri)) {
                    if (parent.getParent() != null) {
                        List<LogicalReference> list =  recurseReferencePromotionPath(reference.getUri(), parent.getParent());
                        if (list.isEmpty()) {
                            // no references were overridden
                            references.add(reference);
                        } else {
                            references.addAll(list);
                        }

                    } else {
                        references.add(reference);
                    }
                }
            }
        }
        return references;
    }

    @SuppressWarnings({"unchecked"})
    private List<LogicalBinding<?>> resetParent(List<LogicalBinding<?>> list, Bindable parent) {
        List<LogicalBinding<?>> newList = new ArrayList<LogicalBinding<?>>();
        for (LogicalBinding<?> binding : list) {
            newList.add(new LogicalBinding(binding.getDefinition(), parent));
        }
        return newList;
    }

}
