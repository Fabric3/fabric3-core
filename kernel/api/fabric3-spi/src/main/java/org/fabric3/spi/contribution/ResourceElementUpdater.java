package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.util.Set;

/**
 * Dynamically updates a resource element contained in contribution and all references to it, including the transitive set of importing contributions,
 * if any.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceElementUpdater<V extends Serializable> {

    /**
     * Updates the resource element with the new value.
     *
     * @param value                  the new value
     * @param contribution           the containing contribution
     * @param dependentContributions the transitive set of dependent contributions
     */
    void update(V value, Contribution contribution, Set<Contribution> dependentContributions);
}
