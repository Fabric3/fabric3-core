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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class MapBuilderImplTestCase extends TestCase {
    private static final DataType JAVA_CLASS = new JavaType(String.class);
    private DocumentBuilder documentBuilder;
    private TransformerRegistry registry;
    private MapBuilderImpl builder;

    @SuppressWarnings({"unchecked"})
    public void testMultiValueElementMapBuild() throws Exception {
        JavaTypeInfo paramInfo = new JavaTypeInfo(String.class);
        List<JavaTypeInfo> paramInfos = new ArrayList<>();
        paramInfos.add(paramInfo);
        paramInfos.add(paramInfo);

        JavaTypeInfo info = new JavaTypeInfo(List.class, paramInfos);
        JavaGenericType type = new JavaGenericType(info);

        List list = Collections.singletonList(String.class);

        registry.getTransformer(TypeConstants.PROPERTY_TYPE, JAVA_CLASS, list, list);
        EasyMock.expectLastCall().andReturn(new MockTransformer()).times(2);
        EasyMock.replay(registry);

        Document document = documentBuilder.newDocument();
        Element values = document.createElement("values");
        document.appendChild(values);

        createValue("key1", "val1", document, values);

        createValue("key2", "val2", document, values);

        ObjectFactory<?> factory = builder.createFactory("test", type, document, getClass().getClassLoader());

        Map<String, String> result = (Map<String, String>) factory.getInstance();
        assertEquals(2, result.size());
        assertEquals("val1", result.get("key1"));
        assertEquals("val2", result.get("key2"));
    }

    @SuppressWarnings({"unchecked"})
    public void testSingleValueElementMapBuild() throws Exception {
        JavaTypeInfo paramInfo = new JavaTypeInfo(String.class);
        List<JavaTypeInfo> paramInfos = new ArrayList<>();
        paramInfos.add(paramInfo);
        paramInfos.add(paramInfo);

        JavaTypeInfo info = new JavaTypeInfo(List.class, paramInfos);
        JavaGenericType type = new JavaGenericType(info);

        List list = Collections.singletonList(String.class);

        registry.getTransformer(TypeConstants.PROPERTY_TYPE, JAVA_CLASS, list, list);
        EasyMock.expectLastCall().andReturn(new MockTransformer()).times(2);
        EasyMock.replay(registry);

        Document document = documentBuilder.newDocument();

        Element values = document.createElement("values");
        document.appendChild(values);

        Element value = document.createElement("value");
        values.appendChild(value);
        Element entry = document.createElement("entry");
        value.appendChild(entry);

        Element mapKey = document.createElement("key");
        mapKey.setTextContent("key1");
        entry.appendChild(mapKey);
        Element mapValue = document.createElement("value");
        mapValue.setTextContent("val1");
        entry.appendChild(mapValue);

        entry = document.createElement("entry");
        value.appendChild(entry);
        mapKey = document.createElement("key");
        mapKey.setTextContent("key2");
        entry.appendChild(mapKey);
        mapValue = document.createElement("value");
        mapValue.setTextContent("val2");
        entry.appendChild(mapValue);

        ObjectFactory<?> factory = builder.createFactory("test", type, document, getClass().getClassLoader());

        Map<String, String> result = (Map<String, String>) factory.getInstance();
        assertEquals(2, result.size());
        assertEquals("val1", result.get("key1"));
        assertEquals("val2", result.get("key2"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        registry = EasyMock.createMock(TransformerRegistry.class);
        builder = new MapBuilderImpl(registry);
    }

    private class MockTransformer implements Transformer<Node, String> {

        public String transform(Node value, ClassLoader loader) throws TransformationException {
            return value.getTextContent();
        }
    }

    private void createValue(String key, String val, Document document, Element values) {
        Element value = document.createElement("value");
        values.appendChild(value);
        Element entry = document.createElement("entry");
        value.appendChild(entry);
        Element mapKey = document.createElement("key");
        mapKey.setTextContent(key);
        entry.appendChild(mapKey);
        Element mapValue = document.createElement("value");
        mapValue.setTextContent(val);
        entry.appendChild(mapValue);
    }


}