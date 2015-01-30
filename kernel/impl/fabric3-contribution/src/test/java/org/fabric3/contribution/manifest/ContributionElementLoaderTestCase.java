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
package org.fabric3.contribution.manifest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
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
            "    <deployable composite='f3:ControllerExtension' environments='production staging' modes='node vm'/>" +
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
        assertTrue(parsedDeployable.getEnvironments().contains("production"));
        assertTrue(parsedDeployable.getEnvironments().contains("staging"));
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
