package org.fabric3.policy.resolver;

import java.util.Set;

import org.fabric3.model.type.definitions.Intent;

/**
 * Holder for provided and aggregated intents.
 */
public class IntentPair {
    private Set<Intent> aggregatedIntents;
    private Set<Intent> providedIntents;

    public IntentPair(Set<Intent> aggregatedIntents, Set<Intent> providedIntents) {
        this.aggregatedIntents = aggregatedIntents;
        this.providedIntents = providedIntents;
    }

    public Set<Intent> getAggregatedIntents() {
        return aggregatedIntents;
    }

    public Set<Intent> getProvidedIntents() {
        return providedIntents;
    }
}
