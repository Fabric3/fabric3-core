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
package org.fabric3.contribution;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.wire.ContributionWireInstantiator;
import org.fabric3.contribution.wire.ContributionWireInstantiatorRegistryImpl;
import org.fabric3.contribution.wire.QNameWireInstantiator;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.host.util.FileHelper;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameExport;
import org.fabric3.spi.contribution.manifest.QNameImport;
import org.fabric3.spi.contribution.manifest.QNameSymbol;

/**
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImplTestCase extends TestCase {
    private static final URI RESOURCE_URI = URI.create("test-resource");
    private static final URI RESOURCE_URI2 = URI.create("test-resource2");
    private static final String IMPORT_EXPORT_QNAME = "test";
    private static final String IMPORT_EXPORT_QNAME2 = "test2";
    private MetaDataStoreImpl store;

    public void testResolve() throws Exception {
        URI uri = URI.create("source");
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME, null);
        List<ContributionWire<?, ?>> wires = store.resolveContributionWires(uri, imprt);
        assertEquals(RESOURCE_URI, wires.get(0).getExportContributionUri());
    }

    public void testResolveContainingResource() throws Exception {
        URI uri = URI.create("resource");
        Contribution contribution = new Contribution(uri);
        QName qname = new QName("foo", "bar");
        QNameSymbol symbol = new QNameSymbol(qname);
        ResourceElement<QNameSymbol, Serializable> element = new ResourceElement<QNameSymbol, Serializable>(symbol);
        URL url = new URL("file://foo");
        Source source = new UrlSource(url);
        Resource resource = new Resource(source, "resource");
        resource.addResourceElement(element);
        contribution.addResource(resource);
        store.store(contribution);
        assertEquals(resource, store.resolveContainingResource(uri, symbol));
    }

    public void testResolveDependentContributions() throws Exception {
        Set<Contribution> contributions = store.resolveDependentContributions(RESOURCE_URI);
        assertEquals(RESOURCE_URI2, contributions.iterator().next().getUri());
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClassLoaderRegistry registry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(registry.getClassLoader(URI.create("resource"))).andReturn(getClass().getClassLoader());
        EasyMock.replay(registry);
        store = new MetaDataStoreImpl(registry, null);
        Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators =
                new HashMap<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>>();
        instantiators.put(QNameImport.class, new QNameWireInstantiator());
        ContributionWireInstantiatorRegistryImpl instantiatorRegistry = new ContributionWireInstantiatorRegistryImpl();
        instantiatorRegistry.setInstantiators(instantiators);
        store.setInstantiatorRegistry(instantiatorRegistry);
        Contribution contribution = new Contribution(RESOURCE_URI);
        ContributionManifest manifest = contribution.getManifest();
        QNameExport export = new QNameExport(IMPORT_EXPORT_QNAME);
        manifest.addExport(export);
        store.store(contribution);

        Contribution contribution2 = new Contribution(RESOURCE_URI2);
        ContributionManifest manifest2 = contribution2.getManifest();
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME, null);
        manifest2.addImport(imprt);
        QNameExport export2 = new QNameExport(IMPORT_EXPORT_QNAME2);
        manifest2.addExport(export2);
        store.store(contribution2);
        List<ContributionWire<?, ?>> wires = store.resolveContributionWires(RESOURCE_URI2, imprt);
        for (ContributionWire<?, ?> wire : wires) {
            contribution2.addWire(wire);
        }

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("target/repository"));
    }

}
