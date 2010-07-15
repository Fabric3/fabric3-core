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
package org.fabric3.spi.policy;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * Holds metadata for intents and policy sets. Some intents and policy sets, such as role-based authorization, require specific configuration (e.g.
 * roles specified by the intent annotation on a component). Metadata is keyed by intent or policy set qualified name.
 *
 * @version $Rev$ $Date$
 */
public class PolicyMetadata {
    private Map<QName, Object> metadata = new HashMap<QName, Object>();

    public void add(QName name, Object data) {
        metadata.put(name, data);
    }

    /**
     * Returns the metadata associated with the qname.
     *
     * @param name the intent/policy set qname
     * @param type the expected metadata type
     * @param <T>  the metadata type parameter
     * @return the metadata or null;
     */
    public <T> T get(QName name, Class<T> type) {
        return type.cast(metadata.get(name));
    }

    /**
     * Returns all metadata.
     *
     * @return the metadata
     */
    public Map<QName, Object> get() {
        return metadata;
    }

    /**
     * Convenience method for adding a collection of metadata.
     *
     * @param metadata the metadata to add
     */
    public void addAll(Map<QName, Object> metadata) {
        this.metadata.putAll(metadata);
    }

}
