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

import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.contribution.util.MockXMLFactory;
import org.fabric3.spi.contribution.ContributionManifest;

/**
 *
 */
public class ContributionElementLoaderExcludesTestCase extends TestCase {
    private static final String XML = "<contribution xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' xmlns:f3='urn:fabric3.org'>" +
            "<f3:scan exclude='foo,bar'/>" +
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
