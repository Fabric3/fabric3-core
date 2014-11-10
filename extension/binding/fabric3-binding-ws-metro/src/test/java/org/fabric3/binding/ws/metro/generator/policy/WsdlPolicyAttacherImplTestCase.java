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
package org.fabric3.binding.ws.metro.generator.policy;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;

/**
 *
 */
public class WsdlPolicyAttacherImplTestCase extends TestCase {

    public void testAttach() throws Exception {
        DocumentBuilderFactory DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
        DocumentBuilder builder = DOCUMENT_FACTORY.newDocumentBuilder();
        ByteArrayInputStream bas = new ByteArrayInputStream(TestPolicy.POLICY.getBytes());

        Document policyDocument = builder.parse(bas);
        WsdlPolicyAttacherImpl attacher = new WsdlPolicyAttacherImpl();

        PolicyExpressionMapping mapping = new PolicyExpressionMapping("id", policyDocument.getDocumentElement());
        mapping.addOperationName("sayHello");
        List<PolicyExpressionMapping> mappings = new ArrayList<>();
        mappings.add(mapping);

        ByteArrayInputStream wsdlStream = new ByteArrayInputStream(TestWsdl.WSDL.getBytes());
        Document wsdl = builder.parse(wsdlStream);
        attacher.attach(wsdl, Collections.<Element>emptyList(), mappings);
        Element element = wsdl.getDocumentElement();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            if (element.getChildNodes().item(i).getNodeName().equals("Policy")) {
                return;
            }
        }
        fail("Policy node not found");
    }


}
