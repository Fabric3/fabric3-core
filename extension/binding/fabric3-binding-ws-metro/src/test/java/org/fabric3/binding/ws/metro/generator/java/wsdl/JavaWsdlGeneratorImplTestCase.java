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
package org.fabric3.binding.ws.metro.generator.java.wsdl;

import java.io.ByteArrayInputStream;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.sun.xml.ws.api.BindingID;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class JavaWsdlGeneratorImplTestCase extends TestCase {
    private JavaWsdlGenerator generator = new JavaWsdlGeneratorImpl();

    public void testWsdlGeneration() throws Exception {
        QName name = new QName("foo", "bar");
        GeneratedArtifacts artifacts = generator.generate(TestEndpoint.class, name, "http://foo.com/service", BindingID.SOAP11_HTTP);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(artifacts.getWsdl().getBytes()));
        Element root = document.getDocumentElement();
        for (int i = 0; i < root.getChildNodes().getLength(); i++) {
            Node node = root.getChildNodes().item(i);
            if ("portType".equals(node.getNodeName())) {
                    return;
            }
        }
        fail("Abstract WSDL elements not found");
    }

    @WebService
    public static interface TestEndpoint {

        void operation(String param);

    }
}