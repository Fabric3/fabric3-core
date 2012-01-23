package org.fabric3.contribution.updater;

import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Include;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementUpdater;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * Dynamically updates a composite in a contribution and all references to it in the contribution and importing contributions. Updated references
 * include composite component implementations and includes.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeResourceElementUpdater implements ResourceElementUpdater<Composite> {

    public void update(Composite newComposite, Contribution contribution, Set<Contribution> dependentContributions) {
        updateComposite(newComposite, contribution);
        QName name = newComposite.getName();
        QNameSymbol symbol = new QNameSymbol(name);
        for (Contribution dependent : dependentContributions) {
            for (ContributionWire<?, ?> wire : dependent.getWires()) {
                if (wire.resolves(symbol)) {
                    updateComposite(newComposite, dependent);
                    break;
                }
            }
        }
    }

    @SuppressWarnings({"VariableNotUsedInsideIf", "unchecked"})
    private void updateComposite(Composite newComposite, Contribution contribution) {
        Composite replaced = null;
        QName name = newComposite.getName();
        // replace the composite in the contribution
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement element : resource.getResourceElements()) {
                Object value = element.getValue();
                if (value instanceof Composite) {
                    Composite oldComposite = (Composite) value;
                    if (oldComposite.getName().equals(name)) {
                        replaced = oldComposite;
                        element.setValue(newComposite);
                        break;
                    }
                }
                if (replaced != null) {
                    break;
                }
            }
            if (replaced != null) {
                break;
            }
        }
        // replace references to the updated composite from other composites, e.g. implementation.composite and includes
        if (replaced != null) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement element : resource.getResourceElements()) {
                    Object value = element.getValue();
                    if (value instanceof Composite) {
                        Composite current = (Composite) value;
                        for (ComponentDefinition component : current.getDeclaredComponents().values()) {
                            if (component.getImplementation() instanceof CompositeImplementation) {
                                CompositeImplementation implementation = (CompositeImplementation) component.getImplementation();
                                if (implementation.getComponentType().getName().equals(name)) {
                                    // replace with the updated composite
                                    implementation.setComponentType(newComposite);
                                }
                            }
                        }
                        for (Include include : current.getIncludes().values()) {
                            if (name.equals(include.getIncluded().getName())) {
                                // replace with the updated composite
                                include.setIncluded(newComposite);
                            }
                        }
                    }
                }
            }

        }
    }
}
