/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.contribution.updater;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Include;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementUpdater;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Dynamically updates a composite in a contribution and all references to it in the contribution and importing contributions. Updated references
 * include composite component implementations and includes.
 */
@EagerInit
public class CompositeResourceElementUpdater implements ResourceElementUpdater<Composite> {

    public Set<ModelObject> update(Composite newComposite, Contribution contribution, Set<Contribution> dependentContributions) {
        Set<ModelObject> set = new HashSet<>();
        updateComposite(newComposite, contribution, set);
        QName name = newComposite.getName();
        QNameSymbol symbol = new QNameSymbol(name);
        for (Contribution dependent : dependentContributions) {
            for (ContributionWire<?, ?> wire : dependent.getWires()) {
                if (wire.resolves(symbol)) {
                    updateComposite(newComposite, dependent, set);
                    break;
                }
            }
        }
        return set;
    }

    public Set<ModelObject> remove(Composite composite, Contribution contribution, Set<Contribution> dependentContributions) {
        Set<ModelObject> set = new HashSet<>();
        QName name = composite.getName();
        URI uri = composite.getContributionUri();
        Composite pointer = new Composite(name, true, uri);
        set.add(composite);
        removeComposite(contribution, name);
        replaceReferences(pointer, contribution, set);

        QNameSymbol symbol = new QNameSymbol(name);
        for (Contribution dependent : dependentContributions) {
            for (ContributionWire<?, ?> wire : dependent.getWires()) {
                if (wire.resolves(symbol)) {
                    replaceReferences(pointer, dependent, set);
                    break;
                }
            }
        }
        return set;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private void removeComposite(Contribution contribution, QName name) {
        List<Resource> resources = contribution.getResources();
        synchronized (resources) {
            for (Resource resource : resources) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getValue() instanceof Composite) {
                        Composite candidate = (Composite) element.getValue();
                        if (candidate.getName().equals(name)) {
                            resources.remove(resource);
                            if (contribution.getLockOwners().contains(name)) {
                                contribution.releaseLock(name);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"VariableNotUsedInsideIf", "unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
    private void updateComposite(Composite newComposite, Contribution contribution, Set<ModelObject> set) {
        QName name = newComposite.getName();
        // replace the composite in the contribution
        boolean replaced = false;
        List<Resource> resources = contribution.getResources();
        synchronized (resources) {
            for (Resource resource : resources) {
                for (ResourceElement element : resource.getResourceElements()) {
                    Symbol symbol = element.getSymbol();
                    if (symbol instanceof QNameSymbol && ((QNameSymbol) symbol).getKey().equals(name)) {
                        replaced = true;
                        element.setValue(newComposite);
                        resource.setState(ResourceState.PROCESSED);
                        set.add(newComposite);
                        break;
                    }
                    if (replaced) {
                        break;
                    }
                }
                if (replaced) {
                    break;
                }
            }
            if (replaced) {
                replaceReferences(newComposite, contribution, set);
            }
        }
    }

    /**
     * Replaces references to the updated composite from other composites in a contribution, e.g. implementation.composite and includes.
     *
     * @param newComposite the updated composite
     * @param contribution the contribution
     * @param set          the collection of modified elements to update
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private void replaceReferences(Composite newComposite, Contribution contribution, Set<ModelObject> set) {
        List<Resource> resources = contribution.getResources();
        synchronized (resources) {
            for (Resource resource : resources) {
                for (ResourceElement element : resource.getResourceElements()) {
                    Object value = element.getValue();
                    if (value instanceof Composite) {
                        Composite current = (Composite) value;
                        for (Component component : current.getDeclaredComponents().values()) {
                            if (component.getComponentType() instanceof Composite) {

                                if (((Composite)component.getComponentType()).getName().equals(newComposite.getName())) {
                                    // replace with the updated composite
                                    //noinspection unchecked
                                    component.getImplementation().setComponentType(newComposite);
                                    set.add(current);
                                }
                            }
                        }
                        for (Include include : current.getIncludes().values()) {
                            if (newComposite.getName().equals(include.getIncluded().getName())) {
                                // replace with the updated composite
                                include.setIncluded(newComposite);
                                set.add(current);
                            }
                        }
                    }
                }
            }
        }
    }

}
