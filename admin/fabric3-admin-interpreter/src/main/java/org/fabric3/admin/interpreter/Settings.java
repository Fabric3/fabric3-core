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
package org.fabric3.admin.interpreter;

import java.io.IOException;
import java.util.Map;

/**
 * Encapsulates persistent settings for the admin interpreter.
 *
 * @version $Rev$ $Date$
 */
public interface Settings {

    /**
     * Adds a domain and its admin address to the collection of configured domains.
     *
     * @param name    the domain name
     * @param address the domain admin address
     */
    void addDomain(String name, String address);

    /**
     * Returns the domain admin address.
     *
     * @param name the domain name
     * @return the domain admin address
     */
    String getDomainAddress(String name);

    /**
     * Returns a map of all configured domains and their admin addresses.
     *
     * @return the map of domains
     */
    Map<String, String> getDomainAddresses();

    /**
     * Loads settings from persistent storage.
     *
     * @throws IOException if there is an error loading the settings
     */
    void load() throws IOException;

    /**
     * Persists settings.
     *
     * @throws IOException if there is an error persisting the settings
     */
    void save() throws IOException;
}
