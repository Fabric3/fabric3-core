/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.management;

import java.net.URI;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.model.type.java.ManagementInfo;

/**
 * Exposes a component to the underlying runtime management framework.
 *
 * @version $Rev: 8369 $ $Date: 2009-12-04 17:26:32 +0100 (Fri, 04 Dec 2009) $
 */
public interface ManagementService {

    /**
     * Exposes a component for management.
     *
     * @param componentUri  the component URI
     * @param info          the management metadata
     * @param objectFactory the object factory responsible for returning the managed component instance
     * @param classLoader   the classloader
     * @throws ManagementException if an error exposing the component is encountered
     */
    void export(URI componentUri, ManagementInfo info, ObjectFactory<?> objectFactory, ClassLoader classLoader) throws ManagementException;

    /**
     * Exposes an instance for management as a system resource.
     *
     * @param name        the management name
     * @param group       the management group
     * @param description the instance description
     * @param instance    the instance
     * @throws ManagementException if an error exposing the instance is encountered
     */
    void export(String name, String group, String description, Object instance) throws ManagementException;

    /**
     * Removes a component from the underlying management framework.
     *
     * @param componentUri the component URI
     * @param info         the management metadata
     * @throws ManagementException if an error removing the component is encountered
     */
    void remove(URI componentUri, ManagementInfo info) throws ManagementException;

    /**
     * Removes an instance from the underlying management framework.
     *
     * @param name  the management name
     * @param group the management group
     * @throws ManagementException if an error removing the component is encountered
     */
    public void remove(String name, String group) throws ManagementException;

}