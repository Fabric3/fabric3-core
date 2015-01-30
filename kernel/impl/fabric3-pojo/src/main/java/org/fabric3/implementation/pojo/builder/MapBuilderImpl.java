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

package org.fabric3.implementation.pojo.builder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.fabric3.spi.model.type.TypeConstants.PROPERTY_TYPE;

/**
 *
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
    public ObjectFactory<Map> createFactory(String name, JavaGenericType type, Document value, ClassLoader classLoader) throws ContainerException {
        List<JavaTypeInfo> typeInfos = type.getTypeInfo().getParameterTypesInfos();
        if (typeInfos.size() < 2) {
            // programming error
            throw new ContainerException("Map properties must have a key and value type");
        }
        Class<?> keyType = typeInfos.get(0).getRawType();
        List<Class<?>> keyTypes = new ArrayList<>();
        keyTypes.add(keyType);
        Class<?> valueType = type.getTypeInfo().getParameterTypesInfos().get(1).getRawType();
        List<Class<?>> valueTypes = new ArrayList<>();
        valueTypes.add(valueType);

        Transformer<Node, ?> keyTransformer = getTransformer(name, PROPERTY_TYPE, new JavaType(keyType), keyTypes);
        Transformer<Node, ?> valueTransformer = getTransformer(name, PROPERTY_TYPE, new JavaType(valueType), valueTypes);

        Map<Object, Object> map = new HashMap<>();
        Element root = value.getDocumentElement();

        Element topValue = normalizeValues(root);
        if (topValue == null) {
            throw new ContainerException("Invalid Map format: no top-level value tag for " + name);
        }

        NodeList keys = topValue.getElementsByTagName("key");
        NodeList values = topValue.getElementsByTagName("value");
        if (keys.getLength() != values.getLength()) {
            throw new ContainerException("Invalid Map format: keys and values must be the same length for " + name);
        } else if (keys.getLength() == 0) {
            throw new ContainerException("Invalid Map format: there must be a key and value node for property " + name);
        }
        for (int i = 0; i < keys.getLength(); i++) {
            Element keyNode = (Element) keys.item(i);
            Element valNode = (Element) values.item(i);
            Object key = keyTransformer.transform(keyNode, classLoader);
            Object val = valueTransformer.transform(valNode, classLoader);
            map.put(key, val);

        }
        return new SingletonObjectFactory<Map>(map);

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
            List<Element> entries = new ArrayList<>();
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