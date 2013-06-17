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
package org.fabric3.transform.property;

import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.spi.transform.TransformationException;


/**
 * Tests String to URI transform.
 */
public class Property2URITransformerTestCase extends BaseTransformTest {

    /**
     * Test for successful transformation from String to URI
     */
    public void testURITransformSuccess() {
        final String uriContent = "xmlns:f3";
        final String xml = "<string_to_uri>" + uriContent + "</string_to_uri>";

        try {
            Property2URITransformer transformer = new Property2URITransformer();
            URI transformedURI = transformer.transform(getNode(xml), getClass().getClassLoader());
            assertNotNull(transformedURI);
            assertEquals(uriContent, transformedURI.toString());
        } catch (TransformationException te) {
            fail("TransformationException : - Should Not Occur" + te);
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }

    /**
     * Test for unsuccessful Conversion from String URI
     */
    public void testURITransformationSuccess() {
        final String errorURIContent = "[[[[]]io9876^^^hasx";
        final String xml = "<string_to_urierror>" + errorURIContent + "</string_to_urierror>";

        try {
            Property2URITransformer transformer = new Property2URITransformer();
            transformer.transform(getNode(xml), getClass().getClassLoader());
            fail("Should not convert to URI");
        } catch (TransformationException te) {
            assertNotNull(te);
            URISyntaxException.class.isAssignableFrom(te.getClass());
        } catch (Exception e) {
            fail("Unexpexcted Exception Should not occur " + e);
        }
    }


}
