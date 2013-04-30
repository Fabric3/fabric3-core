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
package org.fabric3.model.type;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for types that intents and policySets may be associated with.
 */
public abstract class AbstractPolicyAware<P extends ModelObject> extends ModelObject<P> implements PolicyAware {
    private static final long serialVersionUID = -3494285576822641528L;

    private Set<QName> intents = new LinkedHashSet<QName>();
    private Set<QName> policySets = new LinkedHashSet<QName>();
    private Map<QName, Serializable> metadata = new HashMap<QName, Serializable>();

    public Set<QName> getIntents() {
        return intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void setIntents(Set<QName> intents) {
        this.intents = intents;
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public void addIntents(Set<QName> intents) {
        this.intents.addAll(intents);
    }

    public void setPolicySets(Set<QName> policySets) {
        this.policySets = policySets;
    }

    public void addPolicySet(QName policySet) {
        policySets.add(policySet);
    }

    public void addPolicySets(Set<QName> policySets) {
        this.policySets.addAll(policySets);
    }

    public void addMetadata(QName name, Serializable data) {
        metadata.put(name, data);
    }

    public <T> T getMetadata(QName name, Class<T> type) {
        return type.cast(metadata.get(name));
    }

    public Map<QName, Serializable> getMetadata() {
        return metadata;
    }
}
