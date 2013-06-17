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
     * <p/>
     * <p/>
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
