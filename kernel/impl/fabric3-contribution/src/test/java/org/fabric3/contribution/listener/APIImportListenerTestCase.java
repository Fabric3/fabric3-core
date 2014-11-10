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
 */
package org.fabric3.contribution.listener;

import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 *
 */
public class APIImportListenerTestCase extends TestCase {
    private APIImportListener listener;

    private Contribution extensionContribution;
    private Contribution contribution;

    public void testAddImport() throws Exception {
        listener.onInstall(extensionContribution);

        listener.onProcessManifest(contribution);

        assertEquals(1, contribution.getManifest().getImports().size());
    }

    public void testUninstallImport() throws Exception {
        listener.onInstall(extensionContribution);
        listener.onUninstall(extensionContribution);

        listener.onProcessManifest(contribution);

        assertTrue(contribution.getManifest().getImports().isEmpty());
    }

    public void setUp() throws Exception {
        super.setUp();

        listener = new APIImportListener();
        ContributionManifest extensionManifest = new ContributionManifest();
        extensionManifest.setExtension(true);
        PackageInfo info = new PackageInfo("org.fabric3.api.binding.foo.*");
        JavaExport export = new JavaExport(info);
        extensionManifest.addExport(export);

        extensionContribution = new Contribution(URI.create("extension"));
        extensionContribution.setManifest(extensionManifest);

        contribution = new Contribution(URI.create("test"));
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);

    }
}
