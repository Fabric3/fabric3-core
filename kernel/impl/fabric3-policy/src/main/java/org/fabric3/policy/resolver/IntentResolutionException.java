package org.fabric3.policy.resolver;

import java.util.Set;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.spi.generator.policy.PolicyResolutionException;

/**
 *
 */
public class IntentResolutionException extends PolicyResolutionException {
    private static final long serialVersionUID = 812139162659801123L;

    private String intentNames;

    public IntentResolutionException(String message, Set<Intent> intents) {
        super(message);
        StringBuilder builder = new StringBuilder();
        for (Intent intent : intents) {
            builder.append(intent.getName().toString()).append(" ");
        }
        intentNames = builder.toString();
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return message + ":" + intentNames;
    }
}
