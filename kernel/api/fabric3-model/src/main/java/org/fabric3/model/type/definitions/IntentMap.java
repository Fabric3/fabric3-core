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
package org.fabric3.model.type.definitions;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.ModelObject;

/**
 * A policy set intent map.
 *
 * @version $Rev: 9861 $ $Date: 2011-01-17 22:30:33 +0100 (Mon, 17 Jan 2011) $
 */
public final class IntentMap extends ModelObject {
    private static final long serialVersionUID = -1786000484366117318L;
    private QName provides;
    private Set<IntentQualifier> qualifiers = new HashSet<IntentQualifier>();

    public IntentMap(QName provides) {
        this.provides = provides;
    }

    /**
     * Returns the unqualified intent this map provides, which must correspond to an intent in the provides attribute of the parent policy set.
     *
     * @return the unqualified intent
     */
    public QName getProvides() {
        return provides;
    }

    /**
     * Adds an intent qualifier.
     *
     * @param qualifier the qualifier
     */
    public void addQualifier(IntentQualifier qualifier) {
        qualifiers.add(qualifier);
    }

    /**
     * Returns the qualifiers for this map.
     *
     * @return the qualifiers for this map.
     */
    public Set<IntentQualifier> getQualifiers() {
        return qualifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntentMap intentMap = (IntentMap) o;

        return !(provides != null ? !provides.equals(intentMap.provides) : intentMap.provides != null);

    }

    @Override
    public int hashCode() {
        return provides != null ? provides.hashCode() : 0;
    }
}
