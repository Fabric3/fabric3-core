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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transform.property;

import javax.xml.namespace.QName;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.w3c.dom.Node;

/**
 *
 */
public class Property2QNameTransformer implements SingleTypeTransformer<Node, QName> {
    private static final JavaType TARGET = new JavaType(QName.class);

    public DataType getSourceType() {
        return TypeConstants.PROPERTY_TYPE;
    }

    public DataType getTargetType() {
        return TARGET;
    }

    public QName transform(final Node node, ClassLoader loader) throws Fabric3Exception {
        String content = node.getTextContent();
        // see if the content looks like it might reference a namespace
        int index = content.indexOf(':');
        if (index != -1) {
            String prefix = content.substring(0, index);
            String uri = node.lookupNamespaceURI(prefix);
            // a prefix was found that resolved to a namespace - return the associated QName
            if (uri != null) {
                String localPart = content.substring(index + 1);
                return new QName(uri, localPart, prefix);
            }
        }
        return QName.valueOf(content);
    }
}
