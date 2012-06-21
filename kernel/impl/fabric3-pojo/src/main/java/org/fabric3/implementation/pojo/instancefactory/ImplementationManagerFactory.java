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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.instancefactory;

import java.lang.reflect.Type;

import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Creates {@link ImplementationManager}s.
 *
 * @version $Rev$ $Date$
 */
public interface ImplementationManagerFactory {

    /**
     * Creates an instance manager that can be used to create component instances.
     *
     * @return a new instance factory
     */
    ImplementationManager createManager();

    /**
     * Return the implementation class.
     *
     * @return the implementation class
     */
    Class<?> getImplementationClass();

    /**
     * Signals the start of a component configuration update.
     */
    void startUpdate();

    /**
     * Signals when a component configuration update is complete.
     */
    void endUpdate();

    /**
     * Returns a previously added object factory for the injectable site.
     *
     * @param attribute the injection site
     * @return the object factory or null
     */
    ObjectFactory<?> getObjectFactory(Injectable attribute);

    /**
     * Sets an object factory for an injectable.
     *
     * @param injectable    the injection site name
     * @param objectFactory the object factory
     */
    void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory);

    /**
     * Sets an object factory that is associated with a key for an injectable.
     *
     * @param injectable    the injection site
     * @param objectFactory the object factory
     * @param key           the key for Map-based injection sites
     */
    void setObjectFactory(Injectable injectable, ObjectFactory<?> objectFactory, Object key);

    /**
     * Removes an object factory for an injection site.
     *
     * @param injectable the injection site name
     */
    void removeObjectFactory(Injectable injectable);

    /**
     * Returns the type for the injection site
     *
     * @param injectable the injection site
     * @return the required type
     */
    Class<?> getMemberType(Injectable injectable);

    /**
     * Returns the generic type for the injection site
     *
     * @param injectable the injection site
     * @return the required type
     */
    Type getGenericType(Injectable injectable);

}
