package org.fabric3.implementation.java.introspection;

import org.fabric3.spi.introspection.java.JavaValidationFailure;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 *
 */
public class IllegalManagementAttribute extends JavaValidationFailure {
    private String implClass;

    public IllegalManagementAttribute(Class<?> implClass, InjectingComponentType componentType) {
        super(implClass, componentType);
        this.implClass = implClass.getName();
    }

    public String getMessage() {
        return "Implementation class " + implClass + " is marked as a managed component but management is not supported for its scope. " +
                "Management will not be enabled.";
    }

    public String getShortMessage() {
        return "Implementation class " + implClass + " is marked as a managed component but management is not supported for its scope";
    }

}
