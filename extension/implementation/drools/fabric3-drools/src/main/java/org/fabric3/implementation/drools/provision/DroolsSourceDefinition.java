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
package org.fabric3.implementation.drools.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.type.java.InjectableType;

/**
 * @version $Revision$ $Date$
 */
public class DroolsSourceDefinition extends PhysicalSourceDefinition {
    private static final long serialVersionUID = 8851323261546722889L;

    private String identifier;
    private String interfaceName;
    private InjectableType injectableType;

    /**
     * Constructor.
     *
     * @param uri            the source URI
     * @param identifier     the global variable identifier the wire is to be injected into the knowledge session using.
     * @param interfaceName  the interface type
     * @param injectableType the injectable type
     */
    public DroolsSourceDefinition(URI uri, String identifier, String interfaceName, InjectableType injectableType) {
        setUri(uri);
        this.identifier = identifier;
        this.interfaceName = interfaceName;
        this.injectableType = injectableType;
    }

    /**
     * Returns the global variable identifier the wire is to be injected into the knowledge session.
     *
     * @return the global variable identifier the wire is to be injected into the knowledge session
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the full qualified class name of the interface type.
     *
     * @return the full qualified class name of the interface type
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Returns the injectable type.
     *
     * @return the injectable type
     */
    public InjectableType getInjectableType() {
        return injectableType;
    }

}
