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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.introspection.xml;

import javax.xml.namespace.QName;

/**
 * Registry for XML loaders that can parse a StAX input stream and return model objects. Loaders will typically be contributed to the runtime by any extension
 * that needs to handle extension specific information contained in an XML file.
 */
public interface LoaderRegistry extends Loader {
    /**
     * Register a loader. This operation will typically be called by a loader during its initialization.
     *
     * @param element the name of the XML global element that this loader can handle
     * @param loader  a loader that is being contributed to the system
     * @throws IllegalStateException if there is already a loader registered for the supplied element
     */
    void registerLoader(QName element, TypeLoader<?> loader) throws IllegalStateException;

    /**
     * Unregister the loader for the supplied element. This will typically be called by a loader as it is being destroyed. This method simply returns if no
     * loader is registered for that element.
     *
     * @param element the element whose loader should be unregistered
     */
    void unregisterLoader(QName element);

    /**
     * Returns true if the element is registered.
     *
     * @param element the element
     * @return true if the element is registered
     */
    boolean isRegistered(QName element);
}
