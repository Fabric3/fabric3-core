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
package org.fabric3.databinding.jaxb.mapper;

import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 *
 */
public class JAXBQNameMapperImpl implements JAXBQNameMapper {

    public QName deriveQName(Class<?> type) {
        QName name;
        XmlType xmlType = type.getAnnotation(XmlType.class);
        if (xmlType != null) {
            String namespace = xmlType.namespace();
            if ("##default".equals(namespace)) {
                namespace = deriveNamespace(type);
            }
            String localName = xmlType.name();
            if ("##default".equals(localName)) {
                localName = deriveLocalName(type);
            }
            name = new QName(namespace, localName);
        } else {
            String namespace = deriveNamespace(type);
            String localName = deriveLocalName(type);
            name = new QName(namespace, localName);
        }
        return name;
    }

    /**
     * Derives an XML namespace from a Java package according to JAXB rules. For example, org.foo is rendered as http://foo.org/.
     *
     *
     * TODO this is duplicated in the Metro extension
     *
     * @param type the Java type
     * @return the XML namespace
     */
    String deriveNamespace(Class<?> type) {
        Package thePackage = type.getPackage();
        if (thePackage == null) {
            return null;
        }
        String pkg = thePackage.getName();
        String[] tokens = pkg.split("\\.");
        StringBuilder builder = new StringBuilder("http://");
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            builder.append(token);
            if (i != 0) {
                builder.append(".");
            } else {
                builder.append("/");
            }
        }
        return builder.toString();
    }

    /**
     * Derives a local name from the class name by converting the first character to lowercase according to JAXB rules (Section 8.12.1).
     *
     * @param type the class to derive the name from
     * @return the derived name
     */
    private String deriveLocalName(Class<?> type) {
        String localName;
        String simpleName = type.getSimpleName();
        localName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        return localName;
    }

}
