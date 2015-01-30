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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.model.type.java.JavaType;
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
public class ArrayBuilderImplTestCase extends TestCase {
    private static final DataType JAVA_CLASS = new JavaType(String.class);
    private DocumentBuilder documentBuilder;
    private TransformerRegistry registry;
    private ArrayBuilderImpl builder;


    public void testArrayBuild() throws Exception {
        DataType type = new JavaType(String[].class);

        List<Class<?>> list = Collections.singletonList(String.class);

        registry.getTransformer(TypeConstants.PROPERTY_TYPE, JAVA_CLASS, list, list);
        EasyMock.expectLastCall().andReturn(new MockTransformer());
        EasyMock.replay(registry);

        Document document = documentBuilder.newDocument();
        Element values = document.createElement("values");
        document.appendChild(values);
        Element value = document.createElement("value");
        value.setTextContent("test1");
        values.appendChild(value);
        value = document.createElement("value");
        value.setTextContent("test2");
        values.appendChild(value);

        ObjectFactory<?> factory = builder.createFactory("test", type, document, getClass().getClassLoader());

        String[] array = (String[]) factory.getInstance();
        assertEquals(2, array.length);
        assertEquals("test1", array[0]);
        assertEquals("test2", array[1]);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        registry = EasyMock.createMock(TransformerRegistry.class);
        builder = new ArrayBuilderImpl(registry);
    }

    private class MockTransformer implements Transformer<Node, String> {

        public String transform(Node value, ClassLoader loader) throws TransformationException {
            return value.getTextContent();
        }
    }
}