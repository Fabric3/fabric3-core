package org.fabric3.implementation.java.introspection;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Rev$ $Date$
 */
public class IllegalManagementInterface extends ValidationFailure {
    private String interfaze;
    private String implClass;

    public IllegalManagementInterface(String interfaze, String implClass) {
        this.interfaze = interfaze;
        this.implClass = implClass;
    }

    public String getMessage() {
        return "Implementation class " + implClass + " implements the management interface " + interfaze
                + " but instance management is not supported for its scope. Management will not be enabled";
    }

}
