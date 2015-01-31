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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transform.property;

import javax.xml.namespace.QName;

import org.fabric3.api.host.ContainerException;

/**
 * Tests String to QName transform.
 */
public class Property2QNameTransformerTestCase extends BaseTransformTest {

    /**
     * Test of converting String to QName
     */
    public void testQNameTransform() {
        final String Q_NAME = "<string_to_qname>{http://f3.com/ns/fabric/test}f3</string_to_qname>";
        try {
            Property2QNameTransformer transformer = new Property2QNameTransformer();
            final QName qname = transformer.transform(getNode(Q_NAME), getClass().getClassLoader());
            assertNotNull(qname);
            assertEquals("{http://f3.com/ns/fabric/test}f3", qname.toString());
            assertEquals("http://f3.com/ns/fabric/test", qname.getNamespaceURI());
            assertEquals("f3", qname.getLocalPart());
        } catch (ContainerException te) {
            fail("Transform exception should not occur " + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test of converting String to QName
     */
    public void testQNameTransformWithNamespace() {
        final String Q_NAME = "<string_to_qname xmlns:foo='http://f3.com/ns/fabric/test'>foo:f3</string_to_qname>";
        try {
            Property2QNameTransformer transformer = new Property2QNameTransformer();
            final QName qname = transformer.transform(getNode(Q_NAME), getClass().getClassLoader());
            assertNotNull(qname);
            assertEquals("{http://f3.com/ns/fabric/test}f3", qname.toString());
            assertEquals("http://f3.com/ns/fabric/test", qname.getNamespaceURI());
            assertEquals("f3", qname.getLocalPart());
            assertEquals("foo", qname.getPrefix());
        } catch (ContainerException te) {
            fail("Transform exception should not occur " + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test failure converting String to QName
     */
    public void testQNameTransformFailure() {
        final String Q_NAME = "<string_to_qname>{}</string_to_qname>";
        try {
            Property2QNameTransformer transformer = new Property2QNameTransformer();
            transformer.transform(getNode(Q_NAME), null);
            fail("Should not reach here something wrong in [ String2QName ] code");
        } catch (IllegalArgumentException te) {
            assertNotNull(te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

}
