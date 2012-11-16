package org.fabric3.cache.introspection;

import java.lang.reflect.Member;

import org.fabric3.model.type.component.ComponentType;
import org.fabric3.spi.introspection.java.JavaValidationFailure;

/**
 *
 */
public class InvalidCacheSetter extends JavaValidationFailure {
    private String message;

    public InvalidCacheSetter(String message, Member member, ComponentType componentType) {
        super(member, componentType);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
