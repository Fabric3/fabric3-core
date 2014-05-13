/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.api.model.type;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fabric3.api.Namespaces;
import org.oasisopen.sca.Constants;

/**
 * Namespace context with built-in support for the f3 and sca namespaces.
 */
public class F3NamespaceContext implements NamespaceContext {
    private Map<String, String> prefixesToUris = new HashMap<>();
    private Map<String, List<String>> urisToPrefixes = new HashMap<>();

    public F3NamespaceContext() {
        prefixesToUris.put("sca", Constants.SCA_NS);
        prefixesToUris.put("f3", Namespaces.F3);

        List<String> sca = new ArrayList<>();
        sca.add("sca");
        urisToPrefixes.put(Constants.SCA_NS, sca);

        List<String> f3 = new ArrayList<>();
        f3.add("f3");

        urisToPrefixes.put(Namespaces.F3, f3);
    }

    public void add(String prefix, String namespace) {
        prefixesToUris.put(prefix, namespace);
        List<String> prefixes = urisToPrefixes.get(namespace);
        if (prefixes == null) {
            prefixes = new ArrayList<>();
            urisToPrefixes.put(namespace, prefixes);
        }
        prefixes.add(prefix);
    }

    public String getNamespaceURI(String prefix) {
        return prefixesToUris.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        List<String> list = urisToPrefixes.get(namespaceURI);
        return list == null ? null : list.get(0);
    }

    public Iterator getPrefixes(String namespaceURI) {
        List<String> list = urisToPrefixes.get(namespaceURI);
        return list == null ? null : list.iterator();
    }
}
