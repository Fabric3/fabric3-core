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
package org.fabric3.implementation.pojo.objectfactory;

import org.fabric3.spi.objectfactory.InjectionAttributes;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Implementations use a backing collection of {@link ObjectFactory} instances that create a collection of instances for injection on a component,
 * e.g. a multiplicity reference.
 * <p/>
 * Implementations should implement {@link ObjectFactory#getInstance()} in a lock-free manner. The semantics of this contract require that update
 * operations are synchronized. That is, access to {@link #clear()}, {@link #startUpdate()} ()}, and {@link #endUpdate()} can only be made from a
 * single thread from the time {@link #startUpdate()} is called to when {@link #endUpdate()} is invoked. This means that implementations can cache
 * changes made via {@link #addObjectFactory(ObjectFactory, InjectionAttributes)} and  apply them atomically when {@link #endUpdate()} is called.
 * During an update sequence, {@link #getInstance()} can continue to return instances using the non-updated backing collection in a lock-free manner.
 *
 * @param <T> the instance type
 */
public interface MultiplicityObjectFactory<T> extends ObjectFactory<T> {

    /**
     * Adds a constituent object factory.
     *
     * @param objectFactory Constituent object factory
     * @param attributes    the injection attributes
     */
    void addObjectFactory(ObjectFactory<?> objectFactory, InjectionAttributes attributes);

    /**
     * Clears the contents of the object factory
     */
    void clear();

    /**
     * Used to put the factory in the update state.
     */
    void startUpdate();

    /**
     * Used to signal when an update is complete. Note that updates may not have taken place.
     */
    void endUpdate();

}
