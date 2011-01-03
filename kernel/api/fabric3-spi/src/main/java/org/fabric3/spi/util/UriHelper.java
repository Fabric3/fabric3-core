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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.util;

import java.net.URI;

/**
 * Utility methods for handling URIs
 *
 * @version $Rev$ $Date$
 */
public final class UriHelper {

    private UriHelper() {
    }

    /**
     * Returns the base name for a component URI, e.g. 'Bar' for 'sca://foo/Bar'
     *
     * @param uri the URI to parse
     * @return the base name
     */
    public static String getBaseName(URI uri) {
        String s = uri.toString();
        int pos = s.lastIndexOf('/');
        if (pos > -1) {
            return s.substring(pos + 1);
        } else {
            return s;
        }
    }

    public static String getParentName(URI uri) {
        String s = uri.toString();
        int pos = s.lastIndexOf('/');
        if (pos > -1) {
            return s.substring(0, pos);
        } else {
            return null;
        }
    }


    public static URI getDefragmentedName(URI uri) {
        if (uri.getFragment() == null) {
            return uri;
        }
        return URI.create(getDefragmentedNameAsString(uri));
    }

    public static String getDefragmentedNameAsString(URI uri) {
        if (uri.getFragment() == null) {
            return uri.toString();
        }
        String s = uri.toString();
        int pos = s.lastIndexOf('#');
        return s.substring(0, pos);
    }

}
