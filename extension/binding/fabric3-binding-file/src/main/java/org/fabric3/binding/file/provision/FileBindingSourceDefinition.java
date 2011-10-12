/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.file.provision;

import org.fabric3.binding.file.common.Strategy;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;

/**
 * Generated metadata used for attaching a service to a file listener.
 *
 * @version $Revision$ $Date$
 */
public class FileBindingSourceDefinition extends PhysicalSourceDefinition {
    private static final long serialVersionUID = -4154935681094596517L;

    private String location;
    private Strategy strategy;
    private String archiveLocation;
    private String errorLocation;

    public FileBindingSourceDefinition(String location, Strategy strategy, String archiveLocation, String errorLocation) {
        this.location = location;
        this.strategy = strategy;
        this.archiveLocation = archiveLocation;
        this.errorLocation = errorLocation;
        setUri(null);
    }

    public String getLocation() {
        return location;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public String getArchiveLocation() {
        return archiveLocation;
    }

    public String getErrorLocation() {
        return errorLocation;
    }
}
