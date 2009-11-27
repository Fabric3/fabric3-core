/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to QName transform.
 *
 * @version $Rev$ $Date$
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
        } catch (TransformationException te) {
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
        } catch (TransformationException te) {
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
        } catch (TransformationException te) {
            assertNotNull(te);
            assertTrue(IllegalArgumentException.class.isAssignableFrom(te.getCause().getClass()));
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

}
