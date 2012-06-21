/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.host.contribution;

import java.util.List;

/**
 * Base class for exceptions indicating a contribution has failed validation.
 *
 * @version $Rev$ $Date$
 */
public abstract class ValidationException extends InstallException {
    private static final long serialVersionUID = -9097590343387033730L;

    private final List<ValidationFailure> errors;
    private final List<ValidationFailure> warnings;

    /**
     * Constructor that initializes the initial list of errors and warnings.
     *
     * @param errors   the list of errors
     * @param warnings the list of warnings
     */
    protected ValidationException(List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super("Validation errors were found");
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Returns a collection of underlying errors associated with this exception.
     *
     * @return the collection of underlying errors
     */
    public List<ValidationFailure> getErrors() {
        return errors;
    }

    /**
     * Returns a collection of underlying warnings associated with this exception.
     *
     * @return the collection of underlying errors
     */
    public List<ValidationFailure> getWarnings() {
        return warnings;
    }

}
