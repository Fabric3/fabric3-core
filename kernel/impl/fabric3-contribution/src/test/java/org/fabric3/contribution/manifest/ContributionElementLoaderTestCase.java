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

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * @version $Rev$ $Date$
 */
public class ContributionElementLoaderTestCase extends TestCase {
    private static final QName CONTRIBUTION = new QName(SCA_NS, "contribution");
    private static final QName DEPLOYABLE_ELEMENT = new QName(SCA_NS, "deployable");
    private static final QName IMPORT_ELEMENT = new QName(SCA_NS, "import");
    private static final QName EXPORT_ELEMENT = new QName(SCA_NS, "export");
    private static final QName DEPLOYABLE = new QName("test");

    private ContributionElementLoader loader;
    private XMLStreamReader reader;
    private IMocksControl control;

    public void testDispatch() throws Exception {
        ContributionManifest manifest = loader.load(reader, null);
        control.verify();
        assertTrue(manifest.isExtension());
        assertEquals(1, manifest.getDeployables().size());
        assertEquals(DEPLOYABLE, manifest.getDeployables().get(0).getName());
        assertEquals(1, manifest.getExports().size());
        assertEquals(1, manifest.getImports().size());
    }

    @SuppressWarnings({"serial"})
    protected void setUp() throws Exception {
        super.setUp();
        control = EasyMock.createStrictControl();
        LoaderRegistry loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ContributionElementLoader(loaderRegistry);

        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0).atLeastOnce();
        EasyMock.expect(reader.getName()).andReturn(CONTRIBUTION);
        EasyMock.expect(reader.getAttributeValue(EasyMock.eq(Namespaces.F3), EasyMock.eq("extension"))).andReturn("true");
        EasyMock.expect(reader.getAttributeValue(EasyMock.eq(Namespaces.F3), EasyMock.eq("description"))).andReturn("the description");
        EasyMock.expect(reader.getAttributeValue(EasyMock.eq(Namespaces.F3), EasyMock.eq("required-capabilities"))).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(EasyMock.eq(Namespaces.F3), EasyMock.eq("capabilities"))).andReturn(null);
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(), EasyMock.eq("modes"))).andReturn(null);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(DEPLOYABLE_ELEMENT);
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(), EasyMock.eq("composite"))).andReturn("test");
        EasyMock.expect(reader.getNamespaceURI()).andReturn(null);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(DEPLOYABLE_ELEMENT);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(IMPORT_ELEMENT);
        Import contribImport = new Import() {
            public URI getLocation() {
                return null;
            }

            public QName getType() {
                return null;
            }

            public boolean isMultiplicity() {
                return false;
            }

        };
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(Object.class),
                (IntrospectionContext) EasyMock.isNull())).andReturn(contribImport);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(IMPORT_ELEMENT);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(EXPORT_ELEMENT);
        Export contribExport = new Export() {
            public int match(Import contributionImport) {
                return NO_MATCH;
            }

            public QName getType() {
                return null;
            }
        };
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(Object.class), (IntrospectionContext) EasyMock.isNull())).andReturn(contribExport);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(EXPORT_ELEMENT);

        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(CONTRIBUTION);
        EasyMock.replay(loaderRegistry);
        EasyMock.replay(reader);
        control.replay();

    }


}
