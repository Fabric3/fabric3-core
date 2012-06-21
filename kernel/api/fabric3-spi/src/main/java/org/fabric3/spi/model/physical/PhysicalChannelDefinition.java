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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;
import javax.xml.namespace.QName;

/**
 * Used to provision a channel on a runtime.
 *
 * @version $Revision: 7729 $ $Date: 2009-10-01 18:21:22 +0200 (Thu, 01 Oct 2009) $
 */
public class PhysicalChannelDefinition implements Serializable {
    private static final long serialVersionUID = 8681183877136491160L;
    private URI uri;
    private QName deployable;
    private boolean synchronous;
    private boolean replicate;
    private PhysicalChannelBindingDefinition bindingDefinition;

    public PhysicalChannelDefinition(URI uri, QName deployable, boolean synchronous, boolean replicate) {
        this.uri = uri;
        this.deployable = deployable;
        this.synchronous = synchronous;
        this.replicate = replicate;
    }

    /**
     * Returns the channel URI.
     *
     * @return the channel URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the deployable composite this channel is defined in.
     *
     * @return the composite qualified name
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Returns true if this channel synchronously dispatches events to consumers.
     *
     * @return true if this channel synchronously dispatches events to consumers.
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * Returns true if the channel replicates events to all channel instances in a zone.
     *
     * @return true if the channel replicates events to all channel instances in a zone
     */
    public boolean isReplicate() {
        return replicate;
    }


    public PhysicalChannelBindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    public void setBindingDefinition(PhysicalChannelBindingDefinition bindingDefinition) {
        this.bindingDefinition = bindingDefinition;
    }
}