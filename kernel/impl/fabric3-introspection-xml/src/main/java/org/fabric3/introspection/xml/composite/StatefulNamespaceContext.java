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
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.NamespaceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A NamespaceContext used for loaded property default values specified on a component configuration.
 */
public class StatefulNamespaceContext implements NamespaceContext, Serializable {
    private static final long serialVersionUID = -296391955811011867L;

    private Map<String, String> prefixToUri = new HashMap<>();
    private Map<String, List<String>> uriToPrefixes = new HashMap<>();


    public String getNamespaceURI(String prefix) {
        return prefixToUri.get(prefix);
    }

    public String getPrefix(String namespaceURI) {
        List<String> list = uriToPrefixes.get(namespaceURI);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public Iterator getPrefixes(String namespaceURI) {
        List<String> list = uriToPrefixes.get(namespaceURI);
        if (list == null) {
            return Collections.emptyList().iterator();
        }
        return list.iterator();
    }

    public void addNamespace(String prefix, String namespaceURI) {
        prefixToUri.put(prefix, namespaceURI);
        List<String> list = uriToPrefixes.get(namespaceURI);
        if (list == null) {
            list = new ArrayList<>();
            uriToPrefixes.put(namespaceURI, list);
        }
        list.add(prefix);
    }
}
