/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.util;

import java.net.URI;

/**
 * Utility methods for handling URIs
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
