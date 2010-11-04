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
package org.fabric3.contribution.manifest;

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.contribution.MockXMLFactory;
import org.fabric3.spi.contribution.ContributionManifest;

/**
 * @version $Rev$ $Date$
 */
public class ContributionElementLoaderExcludesTestCase extends TestCase {
    private static final String XML = "<contribution xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' xmlns:f3-core='urn:fabric3.org:core'>" +
            "<f3-core:scan exclude='foo,bar'/>" +
            "</contribution>";
    private ContributionElementLoader loader;
    private MockXMLFactory factory;

    public void testExcludes() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(XML.getBytes());
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(stream);
        reader.next();
        ContributionManifest manifest = loader.load(reader, null);
        assertEquals(2, manifest.getScanExcludes().size());
        Pattern exclude1 = manifest.getScanExcludes().get(0);
        Pattern exclude2 = manifest.getScanExcludes().get(0);

        assertTrue(exclude1.matcher("foo").matches() || exclude1.matcher("bar").matches());
        assertTrue(exclude2.matcher("foo").matches() || exclude2.matcher("bar").matches());
    }

    protected void setUp() throws Exception {
        loader = new ContributionElementLoader(null);
        factory = new MockXMLFactory();
    }


}
