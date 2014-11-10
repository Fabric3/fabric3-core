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
package org.fabric3.fabric.domain.instantiator.component;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 */
public class NamespaceHelper {

    /**
     * Copies namespace attributes from a source, recursing its parents, to target node.
     *
     * @param source the source node
     * @param target the target node
     */
    public static void copyNamespaces(Node source, Node target) {
        if (!(target instanceof Element)) {
            return;
        }
        Element targetElement = (Element) target;
        Node parent = source.getParentNode();
        while (parent != null) {
            copyNamespaces(parent, targetElement);
            parent = parent.getParentNode();
        }
        NamedNodeMap attributes = source.getAttributes();
        if (attributes == null || attributes.getLength() == 0) {
            // no attributes
            return;
        }
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            if (!(node instanceof Attr)) {
                continue;
            }
            Attr attr = (Attr) node;
            String prefix = attr.getPrefix();

            // check the schema ns
            if ((prefix == null || prefix.length() == 0) && !"xmlns".equals(attr.getLocalName())) {
                // not a default namespace
                continue;
            } else if (!"xmlns".equals(prefix)) {
                // not a namespace declaration
                continue;
            }
            targetElement.setAttributeNS(attr.getNamespaceURI(), attr.getName(), attr.getNodeValue());
        }
    }
}
