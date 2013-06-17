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
package org.fabric3.contribution.updater;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementUpdater;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * Dynamically updates a composite in a contribution and all references to it in the contribution and importing contributions. Updated references
 * include composite component implementations and includes.
 */
@EagerInit
public class CompositeResourceElementUpdater implements ResourceElementUpdater<Composite> {

    public Set<ModelObject> update(Composite newComposite, Contribution contribution, Set<Contribution> dependentContributions) {
        Set<ModelObject> set = new HashSet<ModelObject>();
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
        Set<ModelObject> set = new HashSet<ModelObject>();
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

    @SuppressWarnings({"VariableNotUsedInsideIf", "unchecked"})
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
    private void replaceReferences(Composite newComposite, Contribution contribution, Set<ModelObject> set) {
        List<Resource> resources = contribution.getResources();
        synchronized (resources) {
            for (Resource resource : resources) {
                for (ResourceElement element : resource.getResourceElements()) {
                    Object value = element.getValue();
                    if (value instanceof Composite) {
                        Composite current = (Composite) value;
                        for (ComponentDefinition component : current.getDeclaredComponents().values()) {
                            if (component.getImplementation() instanceof CompositeImplementation) {
                                CompositeImplementation implementation = (CompositeImplementation) component.getImplementation();
                                if (implementation.getComponentType().getName().equals(newComposite.getName())) {
                                    // replace with the updated composite
                                    implementation.setComponentType(newComposite);
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
