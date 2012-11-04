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
package org.fabric3.model.type.component;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.AbstractPolicyAware;
import org.fabric3.model.type.CapabilityAware;

/**
 * Base binding representation.
 */
public abstract class BindingDefinition extends AbstractPolicyAware implements CapabilityAware {
    private static final long serialVersionUID = 8780407747984243865L;

    private URI targetUri;
    private QName type;
    private String name;

    private Set<String> requiredCapabilities = new HashSet<String>();

    /**
     * Constructor for a binding using the default binding name.
     *
     * @param targetUri the target URI which may be null if not specified
     * @param type      the binding type
     */
    public BindingDefinition(URI targetUri, QName type) {
        this.targetUri = targetUri;
        this.type = type;
    }

    /**
     * Constructor for a binding using a configured binding name.
     *
     * @param name      the binding name
     * @param targetUri the target URI which may be null if not specified
     * @param type      the binding type
     */
    public BindingDefinition(String name, URI targetUri, QName type) {
        this.name = name;
        this.targetUri = targetUri;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public QName getType() {
        return type;
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }

}
