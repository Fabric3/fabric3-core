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
package org.fabric3.host;

/**
 * Namespace URIs used in Fabric3.
 *
 * @version $Rev$ $Date$
 */
public interface Namespaces {

    /**
     * Namespace URI used for core Fabric3. Intended usage for map keys in composite files etc. Recommended prefix f3-core.
     */
    String CORE = "urn:fabric3.org:core";

    /**
     * Namespace URI used for fabric3 binding extensions. Recommended prefix f3-binding.
     */
    String BINDING = "urn:fabric3.org:binding";

    /**
     * Namespace URI used for fabric3 implementation extensions. Recommended prefix f3-implementation.
     */
    String IMPLEMENTATION = "urn:fabric3.org:implementation";

    /**
     * Namespace URI used for fabric3 policy extensions. Recommended prefix f3-policy.
     */
    String POLICY = "urn:fabric3.org:policy";

    /**
     * Namespace URI used for miscellaneous extensions. Recommended prefix f3-other.
     */
    String OTHER = "urn:fabric3.org:other";

    /**
     * Namespace URI for synthesized artifacts. Generally not visible to end-user applications.
     */
    String SYNTHESIZED = "urn:fabric3.org:synthesized";
}
