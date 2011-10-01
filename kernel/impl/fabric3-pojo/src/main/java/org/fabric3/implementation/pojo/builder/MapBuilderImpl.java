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

package org.fabric3.implementation.pojo.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

import static org.fabric3.spi.model.type.xsd.XSDConstants.PROPERTY_TYPE;

/**
 * @version $Rev$ $Date$
 */
public class MapBuilderImpl extends AbstractPropertyBuilder implements MapBuilder {
    private DocumentBuilder documentBuilder;

    public MapBuilderImpl(@Reference TransformerRegistry registry) {
        super(registry);
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public ObjectFactory<Map> createFactory(String name, JavaGenericType type, Document value, ClassLoader classLoader)
            throws PropertyTransformException {
        try {
            List<JavaTypeInfo> typeInfos = type.getLogical().getParameterTypesInfos();
            if (typeInfos.size() < 2) {
                // programming error
                throw new PropertyTransformException("Map properties must have a key and value type");
            }
            Class<?> keyType = typeInfos.get(0).getRawType();
            List<Class<?>> keyTypes = new ArrayList<Class<?>>();
            keyTypes.add(keyType);
            Class<?> valueType = type.getLogical().getParameterTypesInfos().get(1).getRawType();
            List<Class<?>> valueTypes = new ArrayList<Class<?>>();
            valueTypes.add(valueType);

            Transformer<Node, ?> keyTransformer = getTransformer(name, PROPERTY_TYPE, new JavaClass(keyType), keyTypes);
            Transformer<Node, ?> valueTransformer = getTransformer(name, PROPERTY_TYPE, new JavaClass(valueType), valueTypes);

            Map<Object, Object> map = new HashMap<Object, Object>();
            Element root = value.getDocumentElement();

            Element topValue = normalizeValues(root);
            if (topValue == null) {
                throw new PropertyTransformException("Invalid Map format: no top-level value tag for " + name);
            }

            NodeList keys = topValue.getElementsByTagName("key");
            NodeList values = topValue.getElementsByTagName("value");
            if (keys.getLength() != values.getLength()) {
                throw new PropertyTransformException("Invalid Map format: keys and values must be the same length for " + name);
            } else if (keys.getLength() == 0) {
                throw new PropertyTransformException("Invalid Map format: there must be a key and value node for property " + name);
            }
            for (int i = 0; i < keys.getLength(); i++) {
                Element keyNode = (Element) keys.item(i);
                Element valNode = (Element) values.item(i);
                Object key = keyTransformer.transform(keyNode, classLoader);
                Object val = valueTransformer.transform(valNode, classLoader);
                map.put(key, val);

            }
            return new SingletonObjectFactory<Map>(map);
        } catch (TransformationException e) {
            throw new PropertyTransformException("Unable to transform property value: " + name, e);
        }

    }

    /**
     * Combines multiple &lt;value&gt; elements containing Map entries into a single &lt;value&gt; element;
     *
     * @param root the root property node
     * @return the normalized value or null if the property format is invalid
     */
    private Element normalizeValues(Element root) {
        NodeList nodes = root.getChildNodes();
        if (nodes.getLength() == 0) {
            // no child value elements
            return null;
        } else if (nodes.getLength() == 1) {
            if (nodes.item(0).getNodeName().equals("value")) {
                // single value, return it
                return (Element) nodes.item(0);
            } else {
                return null;
            }
        } else {
            // combine the multiple value elements
            List<Element> entries = new ArrayList<Element>();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals("value")) {
                    entries.add((Element) nodes.item(i).getFirstChild());
                }
            }
            if (entries.isEmpty()) {
                return null;
            }

            Document document = documentBuilder.newDocument();
            Element newRoot = document.createElement("value");
            document.appendChild(newRoot);
            for (Element entry : entries) {
                document.adoptNode(entry);
                newRoot.appendChild(entry);
            }
            return newRoot;
        }
    }


}