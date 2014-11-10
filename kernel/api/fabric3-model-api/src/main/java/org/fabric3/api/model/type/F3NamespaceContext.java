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
