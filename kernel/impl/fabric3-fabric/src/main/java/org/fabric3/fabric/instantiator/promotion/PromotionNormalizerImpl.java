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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the PromotionNormalizer.
 * <p/>
 * The service promotion normalization algorithm works as follows: <li>A reverse-ordered list of services is constructed by walking the service
 * promotion hierarchy from a leaf component to the domain component. The leaf service is added as the last list entry.
 * <p/>
 * <li>The list is iterated in order, starting with the service nearest the domain level.
 * <p/>
 * <li>For each entry, bindings are added or replaced (according to the overide setting for the service), policies added, a service contract set if
 * not defined, and the leaf component set as the leaf parent. <li>
 * <p/>
 * </ul> The reference promotion algorithm works as follows: <li> A reverse-ordered list of references is constructed by walking the reference
 * promotion hierarchy from a leaf component to the domain component. The leaf reference is added as the last list entry.
 * <p/>
 * <li>The list is iterated in order, starting with the reference nearest the domain level.
 * <p/>
 * <li>For each entry, bindings are added or replaced (according to the overide setting for the reference), policies added and a service contract set
 * if not defined
 * <p/>
 * <li>The list is iterated a second time and wires for references are examined with their targets pushed down to the next (child) level in the
 * hierarchy.
 *
 * @version $Rev$ $Date$
 */
public class PromotionNormalizerImpl implements PromotionNormalizer {

    public void normalize(LogicalComponent<?> component, InstantiationContext context) {
        normalizeServicePromotions(component);
        normalizeReferenceAndWirePromotions(component);
    }

    private void normalizeServicePromotions(LogicalComponent<?> component) {
        for (LogicalService service : component.getServices()) {
            LinkedList<LogicalService> services = new LinkedList<LogicalService>();
            // add the leaf service as the last element
            services.add(service);
            getPromotionHierarchy(service, services);
            if (services.isEmpty()) {
                continue;
            }
            processServicePromotions(services);
        }
    }

    private void normalizeReferenceAndWirePromotions(LogicalComponent<?> component) {
        for (LogicalReference reference : component.getReferences()) {
            LinkedList<LogicalReference> references = new LinkedList<LogicalReference>();
            // add the leaf (promoted) reference as the last element
            references.add(reference);
            getPromotionHierarchy(reference, references);
            if (references.isEmpty()) {
                continue;
            }
            processReferencePromotions(references);
            processWirePromotions(references);
        }
    }

    /**
     * Processes the service promotion hierarchy by updating bindings, policies, and the service contract.
     *
     * @param services the sorted service promotion hierarchy
     */
    private void processServicePromotions(LinkedList<LogicalService> services) {
        if (services.size() < 2) {
            // no promotion evaluation needed
            return;
        }
        LogicalService leafService = services.getLast();
        LogicalComponent<?> leafComponent = leafService.getParent();
        List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
        ServiceContract contract = leafService.getDefinition().getServiceContract();
        Set<QName> intents = new HashSet<QName>();
        Set<QName> policySets = new HashSet<QName>();

        for (LogicalService service : services) {
            if (service.getDefinition().getServiceContract() == null) {
                service.setServiceContract(contract);
            }
            // TODO determine if bindings should be overriden - for now, override
            if (service.getBindings().isEmpty()) {
                service.overrideBindings(bindings);
            } else {
                bindings = new ArrayList<LogicalBinding<?>>();
                bindings.addAll(service.getBindings());
            }
            if (service.getIntents().isEmpty()) {
                service.addIntents(intents);
            } else {
                intents = new HashSet<QName>();
                intents.addAll(service.getIntents());
            }
            if (service.getPolicySets().isEmpty()) {
                service.addPolicySets(policySets);
            } else {
                policySets = new HashSet<QName>();
                policySets.addAll(service.getPolicySets());
            }
            service.setLeafComponent(leafComponent);
            service.setLeafService(leafService);
        }

    }

    /**
     * Processes the reference promotion hierarchy by updating bindings, policies, and the service contract.
     *
     * @param references the sorted reference promotion hierarchy
     */
    private void processReferencePromotions(LinkedList<LogicalReference> references) {
        if (references.size() < 2) {
            // no promotion evaluation needed
            return;
        }
        LogicalReference leafReference = references.getLast();
        List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
        ServiceContract contract = leafReference.getDefinition().getServiceContract();
        Set<QName> intents = new HashSet<QName>();
        Set<QName> policySets = new HashSet<QName>();

        for (LogicalReference reference : references) {
            if (reference.getDefinition().getServiceContract() == null) {
                reference.setServiceContract(contract);
            }
            // TODO determine if bindings should be overriden - for now, override
            if (reference.getBindings().isEmpty()) {
                reference.overrideBindings(bindings);
            } else {
                bindings = new ArrayList<LogicalBinding<?>>();
                bindings.addAll(reference.getBindings());
            }
            if (reference.getIntents().isEmpty()) {
                reference.addIntents(intents);
            } else {
                intents = new HashSet<QName>();
                intents.addAll(reference.getIntents());
            }
            if (reference.getPolicySets().isEmpty()) {
                reference.addPolicySets(policySets);
            } else {
                policySets = new HashSet<QName>();
                policySets.addAll(reference.getPolicySets());
            }
        }

    }

    /**
     * Processes the wiring hierarchy by pushing wires down to child components.
     *
     * @param references the sorted reference promotion hierarchy
     */
    // TODO handle wire addition
    private void processWirePromotions(LinkedList<LogicalReference> references) {
        if (references.size() < 2) {
            // no promotion evaluation needed
            return;
        }
        List<LogicalService> newTargets = new ArrayList<LogicalService>();

        for (LogicalReference reference : references) {
            LogicalCompositeComponent composite = reference.getParent().getParent();
            if (!newTargets.isEmpty()) {
                List<LogicalWire> newWires = new ArrayList<LogicalWire>();
                for (LogicalService target : newTargets) {
                    QName deployable = composite.getDeployable();
                    LogicalWire newWire = new LogicalWire(reference.getParent(), reference, target, deployable);
                    newWires.add(newWire);
                }
                composite.overrideWires(reference, newWires);
                newTargets = new ArrayList<LogicalService>();
            }
            for (LogicalWire wire : reference.getWires()) {
                // currently only supports overriding wires; wire additions must also be supported
                LogicalService target = wire.getTarget();
                newTargets.add(target);
            }
        }

    }


    /**
     * Updates the list of services with the promotion hierarchy for the given service. The list is populated in reverse order so that the leaf
     * (promoted) service is stored last.
     *
     * @param service  the current service to ascend from
     * @param services the list
     */
    private void getPromotionHierarchy(LogicalService service, LinkedList<LogicalService> services) {
        LogicalComponent<CompositeImplementation> parent = service.getParent().getParent();
        URI serviceUri = service.getUri();
        for (LogicalService promotion : parent.getServices()) {
            URI targetUri = promotion.getPromotedUri();
            if (targetUri.getFragment() == null) {
                // no service specified
                if (targetUri.equals(UriHelper.getDefragmentedName(serviceUri))) {
                    services.addFirst(promotion);
                    if (parent.getParent() != null) {
                        getPromotionHierarchy(promotion, services);
                    }
                }

            } else {
                if (targetUri.equals(serviceUri)) {
                    services.addFirst(promotion);
                    if (parent.getParent() != null) {
                        getPromotionHierarchy(promotion, services);
                    }
                }
            }
        }
    }

    /**
     * Updates the list of references with the promotion hierarchy for the given reference. The list is populated in reverse order so that the leaf
     * (promoted) reference is stored last.
     *
     * @param reference  the current service to ascend from
     * @param references the list
     */
    private void getPromotionHierarchy(LogicalReference reference, LinkedList<LogicalReference> references) {
        LogicalComponent<CompositeImplementation> parent = reference.getParent().getParent();
        URI referenceUri = reference.getUri();
        for (LogicalReference promotion : parent.getReferences()) {
            List<URI> promotedUris = promotion.getPromotedUris();
            for (URI promotedUri : promotedUris) {
                if (promotedUri.getFragment() == null) {
                    if (promotedUri.equals(UriHelper.getDefragmentedName(referenceUri))) {
                        references.addFirst(promotion);
                        if (parent.getParent() != null) {
                            getPromotionHierarchy(promotion, references);
                        }
                    }
                } else {
                    if (promotedUri.equals(referenceUri)) {
                        references.addFirst(promotion);
                        if (parent.getParent() != null) {
                            getPromotionHierarchy(promotion, references);
                        }
                    }
                }
            }

        }
    }

}
