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
package org.fabric3.binding.jms.spi.runtime;

/**
 * Defines JMS constants.
 *
 * @version $Rev$ $Date$
 */
public interface JmsConstants {

    /**
     * Header used to specify the service operation name being invoked.
     */
    String OPERATION_HEADER = "scaOperationName";

    /**
     * Header used to determine if a response is a fault.
     */
    String FAULT_HEADER = "f3Fault";

    /**
     * Identifies the default configured non-XA connection factory
     */
    String DEFAULT_CONNECTION_FACTORY = "default";

    /**
     * Identifies the default configured XA-enabled connection factory
     */
    String DEFAULT_XA_CONNECTION_FACTORY = "xaDefault";

    /**
     * No caching of JMS objects
     */
    int CACHE_NONE = 0;

    /**
     * JMS connection caching
     */
    int CACHE_CONNECTION = 1;

    /**
     * Caching of all JMS objects
     */
    int CACHE_ADMINISTERED_OBJECTS = 2;

}
