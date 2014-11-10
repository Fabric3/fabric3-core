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
import java.io.InputStream;
import java.util.List;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.fabric3.api.host.Version;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class OSGiManifestHandlerTestCase extends TestCase {
    private static final String MANIFEST = "Manifest-Version: 1.0\n" +
            "Created-By: test\n" +
            "Import-Package: org.fabric3.foo;resolution:=required,org.fabric3.bar;resolution:=optional,org.fabric3.baz;version" +
            " =\"[1.0.0, 2.0.0)\"\n" +
            "Export-Package: org.fabric3.export1;version=\"1.1.1.1\",org.fabric3.export2;version=\"2.2.2.2\";uses:=\"foo.com, bar.com\"\n";

    private static final String MANIFEST2 = "Manifest-Version: 1.0\n" +
            "Created-By: test\n" +
            "Import-Package: org.fabric3.foo\n" +
            "Export-Package: org.fabric3.export\n";

    private static final String MANIFEST3 = "Manifest-Version: 1.0\n" +
            "Created-By: test\n" +
            "Import-Package: org.fabric3.foo1, org.fabric3.foo2\n" +
            "Export-Package: org.fabric3.export1, org.fabric3.export2\n";

    private OSGiManifestHandler handler = new OSGiManifestHandler();

    public void testHeaderParse() throws Exception {
        Manifest jarManifest = new Manifest(new ByteArrayInputStream(MANIFEST.getBytes()));
        ContributionManifest manifest = new ContributionManifest();
        IntrospectionContext context = new DefaultIntrospectionContext();
        handler.processManifest(manifest, jarManifest, context);

        assertFalse(context.hasErrors());

        assertEquals(3, manifest.getImports().size());
        JavaImport first = (JavaImport) manifest.getImports().get(0);
        assertEquals("org.fabric3.foo", first.getPackageInfo().getName());
        assertTrue(first.getPackageInfo().isRequired());

        JavaImport second = (JavaImport) manifest.getImports().get(1);
        assertEquals("org.fabric3.bar", second.getPackageInfo().getName());
        assertFalse(second.getPackageInfo().isRequired());

        JavaImport third = (JavaImport) manifest.getImports().get(2);
        assertEquals("org.fabric3.baz", third.getPackageInfo().getName());
        assertEquals(new Version("1.0.0"), third.getPackageInfo().getMinVersion());
        assertTrue(third.getPackageInfo().isMinInclusive());
        assertEquals(new Version("2.0.0"), third.getPackageInfo().getMaxVersion());
        assertFalse(third.getPackageInfo().isMaxInclusive());

        assertEquals(2, manifest.getExports().size());
        JavaExport firstExport = (JavaExport) manifest.getExports().get(0);
        assertEquals("org.fabric3.export1", firstExport.getPackageInfo().getName());
        assertEquals(new Version("1.1.1.1"), firstExport.getPackageInfo().getMinVersion());

        JavaExport secondExport = (JavaExport) manifest.getExports().get(1);
        assertEquals("org.fabric3.export2", secondExport.getPackageInfo().getName());
        assertEquals(new Version("2.2.2.2"), secondExport.getPackageInfo().getMinVersion());

    }

    public void testPackage() throws Exception {
        IntrospectionContext context = new DefaultIntrospectionContext();
        InputStream resourceAsStream = new ByteArrayInputStream(MANIFEST2.getBytes());
        Manifest jarManifest = new Manifest(resourceAsStream);
        ContributionManifest manifest = new ContributionManifest();
        handler.processManifest(manifest, jarManifest, context);

        List<Export> exports = manifest.getExports();

        Import dummyImport = manifest.getImports().get(0);

        for (Export export : exports) {
            export.match(dummyImport);
        }
    }

    public void testMultipleImportsExports() throws Exception {
        IntrospectionContext context = new DefaultIntrospectionContext();
        InputStream resourceAsStream = new ByteArrayInputStream(MANIFEST3.getBytes());
        Manifest jarManifest = new Manifest(resourceAsStream);
        ContributionManifest manifest = new ContributionManifest();
        handler.processManifest(manifest, jarManifest, context);

        assertEquals(2, manifest.getImports().size());
        assertEquals(2, manifest.getExports().size());
    }


}
