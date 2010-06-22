package org.fabric3.implementation.java.introspection;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class IllegalManagementAttribute extends ValidationFailure {
    private String implClass;

    public IllegalManagementAttribute(String implClass) {
        this.implClass = implClass;
    }

    public String getMessage() {
        return "Implementation class " + implClass + " is marked as a managed component but instance management is not supported for its scope. " +
                "Management will not be enabled";
    }

}
