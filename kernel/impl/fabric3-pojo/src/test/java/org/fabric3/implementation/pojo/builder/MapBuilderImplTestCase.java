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
import org.fabric3.spi.model.type.xsd.XSDConstants;
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

        registry.getTransformer(XSDConstants.PROPERTY_TYPE, JAVA_CLASS, list, list);
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

        registry.getTransformer(XSDConstants.PROPERTY_TYPE, JAVA_CLASS, list, list);
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