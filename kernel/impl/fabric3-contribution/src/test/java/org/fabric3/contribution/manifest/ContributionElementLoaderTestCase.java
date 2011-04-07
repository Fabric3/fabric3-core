/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class ContributionElementLoaderTestCase extends TestCase {
    private static final QName DEPLOYABLE = new QName("urn:fabric3.org", "ControllerExtension");
    private static final Capability REQUIRED_CAPABILITY = new Capability("some-required-capability");
    private static final Capability PROVIDED_CAPABILITY = new Capability("some-provided-capability");

    private static final String XML = "<contribution xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'\n" +
            "              xmlns:f3='urn:fabric3.org'" +
            "              f3:extension='true'" +
            "              f3:description='JMS extension'>" +
            "    <import.java package='javax.transaction' version='1.1.0'/>" +
            "    <export.java package='org.fabric3.binding.jms.spi.common' version='1.8'/>" +
            "    <deployable composite='f3:ControllerExtension' modes='controller vm'/>" +
            "    <f3:provides name='some-extension'/>" +
            "    <f3:requires.capability name='some-required-capability'/>" +
            "    <f3:provides.capability name='some-provided-capability'/>" +
            "</contribution>";


    private ContributionElementLoader loader;
    private XMLStreamReader reader;
    private LoaderRegistry registry;

    public void testLoad() throws Exception {
        PackageInfo info = new PackageInfo("org.fabric3");
        registry.load(EasyMock.isA(XMLStreamReader.class), EasyMock.eq(Object.class), EasyMock.isA(IntrospectionContext.class));
        JavaImport javaImport = new JavaImport(info);
        EasyMock.expectLastCall().andReturn(javaImport);
        registry.load(EasyMock.isA(XMLStreamReader.class), EasyMock.eq(Object.class), EasyMock.isA(IntrospectionContext.class));
        JavaExport javaExport = new JavaExport(info);
        EasyMock.expectLastCall().andReturn(javaExport);
        registry.load(EasyMock.isA(XMLStreamReader.class), EasyMock.eq(Object.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andReturn(new ProvidesDeclaration("some-extension"));

        EasyMock.replay(registry);
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        ContributionManifest manifest = loader.load(reader, context);

        Deployable parsedDeployable = manifest.getDeployables().get(0);
        assertEquals(DEPLOYABLE, parsedDeployable.getName());
        assertTrue(parsedDeployable.getRuntimeModes().contains(RuntimeMode.VM));
        assertTrue(parsedDeployable.getRuntimeModes().contains(RuntimeMode.CONTROLLER));
        assertFalse(parsedDeployable.getRuntimeModes().contains(RuntimeMode.PARTICIPANT));
        assertTrue(manifest.getImports().contains(javaImport));
        assertTrue(manifest.getExports().contains(javaExport));
        assertTrue(manifest.getRequiredCapabilities().contains(REQUIRED_CAPABILITY));
        assertTrue(manifest.getProvidedCapabilities().contains(PROVIDED_CAPABILITY));
        assertTrue(manifest.isExtension());
        assertFalse(context.hasErrors());

        EasyMock.verify(registry);
    }

    protected void setUp() throws Exception {
        registry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ContributionElementLoader(registry);
        ByteArrayInputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }


}
