package org.fabric3.channel.introspection;

import java.lang.reflect.Member;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.spi.introspection.java.JavaValidationFailure;

/**
 *
 */
public class InvalidChannelContextType extends JavaValidationFailure {
    private String message;

    public InvalidChannelContextType(String message, Member member, ComponentType componentType) {
        super(member, componentType);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
