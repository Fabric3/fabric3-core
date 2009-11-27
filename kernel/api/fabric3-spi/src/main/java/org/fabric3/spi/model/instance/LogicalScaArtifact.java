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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.model.instance;

import java.io.Serializable;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Super class for all logical SCA artifacts.
 *
 * @version $Rev$ $Date$
 */
public abstract class LogicalScaArtifact<P extends LogicalScaArtifact<?>> implements Serializable {
    private static final long serialVersionUID = 3937960041374196627L;
    private final URI uri;
    private final P parent;
    private final QName type;
    private Set<QName> intents = new LinkedHashSet<QName>();
    private Set<QName> policySets = new LinkedHashSet<QName>();

    /**
     * @param uri    URI of the SCA artifact.
     * @param parent Parent of the SCA artifact.
     * @param type   Type of this artifact.
     */
    public LogicalScaArtifact(final URI uri, final P parent, final QName type) {
        this.uri = uri;
        this.parent = parent;
        this.type = type;
    }

    /**
     * Returns the uri.
     *
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return Type of this SCA artifact.
     */
    public QName getType() {
        return type;
    }

    /**
     * @return Parent of this SCA artifact.
     */
    public final P getParent() {
        return parent;
    }

    public String toString() {
        if (uri == null) {
            return "";
        }
        return uri.toString();
    }

    public Set<QName> getIntents() {
        return intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public void addIntents(Set<QName> intents) {
        this.intents.addAll(intents);
    }

    public void addPolicySet(QName policySet) {
        policySets.add(policySet);
    }

    public void removePolicySet(QName policySet) {
        policySets.remove(policySet);
    }

    public void addPolicySets(Set<QName> policySets) {
        this.policySets.addAll(policySets);
    }


}
