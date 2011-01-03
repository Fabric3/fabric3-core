/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.instantiator;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.domain.AssemblyFailure;

/**
 * A context used during logical component instantiation to record errors.
 *
 * @version $Rev$ $Date$
 */
public class InstantiationContext {
    private List<AssemblyFailure> errors = new ArrayList<AssemblyFailure>();

    /**
     * Returns true if the instantiation operation detected any fatal errors.
     *
     * @return true if the instantiation operation has detected any fatal errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the list of fatal errors detected during the instantiation operation.
     *
     * @return the list of fatal errors detected during the instantiation operation
     */
    public List<AssemblyFailure> getErrors() {
        return errors;
    }

    /**
     * Add a fatal error to the instantiation context.
     *
     * @param error the fatal error that has been found
     */
    public void addError(AssemblyFailure error) {
        errors.add(error);
    }

    /**
     * Add a collection of fatal errors to the instantiation context.
     *
     * @param errors the fatal errors that have been found
     */
    public void addErrors(List<AssemblyFailure> errors) {
        this.errors.addAll(errors);
    }

}
