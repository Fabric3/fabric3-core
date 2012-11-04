/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.xsd.XSDConstants;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
public class ArrayBuilderImplTestCase extends TestCase {
    private static final DataType<?> JAVA_CLASS = new JavaClass<String>(String.class);
    private DocumentBuilder documentBuilder;
    private TransformerRegistry registry;
    private ArrayBuilderImpl builder;


    public void testArrayBuild() throws Exception {
        DataType<?> type = new JavaClass<String[]>(String[].class);

        List list = Collections.singletonList(String.class);

        registry.getTransformer(XSDConstants.PROPERTY_TYPE, JAVA_CLASS, list, list);
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