/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.contribution.wire;

import java.net.URI;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * A registry used to dispatch to ContributionWireInstantiators.
 * <p/>
 * This is is required since the kernel does not support reinjection of multiplicity references on Singleton components (it does, however, support
 * reinjection of non-multiplicity references, which is done with this service).
 */
public interface ContributionWireInstantiatorRegistry {

    /**
     * Dispatches to the instantiator to create the wire.
     *
     * @param imprt     the wire's import
     * @param export    the wire's export
     * @param importUri the importing contribution URI
     * @param exportUri the exporting contribution URI
     * @param <I>       the import type
     * @param <E>       the export type
     * @return the ContributionWire
     */
    <I extends Import, E extends Export> ContributionWire<I, E> instantiate(I imprt, E export, URI importUri, URI exportUri);

}
