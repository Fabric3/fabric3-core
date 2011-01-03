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
*/
package org.fabric3.transform.property;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.xsd.XSDConstants;
import org.fabric3.spi.transform.SingleTypeTransformer;
import org.fabric3.spi.transform.TransformationException;

/**
 * @version $Rev$ $Date$
 */
public class Property2QNameTransformer implements SingleTypeTransformer<Node, QName> {
    private static final JavaClass<QName> TARGET = new JavaClass<QName>(QName.class);

    public DataType<?> getSourceType() {
        return XSDConstants.PROPERTY_TYPE;
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public QName transform(final Node node, ClassLoader loader) throws TransformationException {
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
        try {
            return QName.valueOf(content);
        } catch (IllegalArgumentException ie) {
            throw new TransformationException("Unable to transform on QName ", ie);
        }
    }
}
