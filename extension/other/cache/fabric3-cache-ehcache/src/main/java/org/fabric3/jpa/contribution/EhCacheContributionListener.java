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
package org.fabric3.jpa.contribution;

import org.oasisopen.sca.annotation.Property;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;

/**
 * Adds an implicit import of the EhCache contribution extension into any contribution using JPA on a runtime configured to use EhCache. This is
 * necessary as EhCache's use of CGLIB for generating proxies requires that EhCache classes be visible from the
 * classloader that loaded a particular entity (i.e. the application classloader). If a EhCache is explicitly imported in a contribution manifest
 * (sca-contribution.xml), it is used instead.
 */
public class EhCacheContributionListener implements ContributionServiceListener {
    private JavaImport ehCacheImport;
    private boolean noImplicitImport;

    public EhCacheContributionListener() {
        PackageInfo hibernateInfo = new PackageInfo("net.sf.ehcache.*");
        ehCacheImport = new JavaImport(hibernateInfo);
    }

    @Property(required = false)
    public void setNoImplicitImport(boolean noImplicitImport) {
        this.noImplicitImport = noImplicitImport;
    }

    public void onProcessManifest(Contribution contribution) {
        if (noImplicitImport || contribution.getManifest().isExtension()) {
            // implicitly import disabled or the contribution is an extension
            return;
        }
        boolean ehCacheImported = false;
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            if (imprt instanceof JavaImport) {
                JavaImport contributionImport = (JavaImport) imprt;
                String name = contributionImport.getPackageInfo().getName();
                if (name.equals("net.sf.ehcache.*")) {
                    // already explicitly imported
                    ehCacheImported = true;
                }
            }
        }
        // Make sure ehcache package is always imported
        if (!ehCacheImported) {
            manifest.addImport(ehCacheImport);
        }
    }

    public void onStore(Contribution contribution) {
        // no-op
    }

    public void onInstall(Contribution contribution) {
        // no-op
    }

    public void onUpdate(Contribution contribution) {
        // no-op
    }

    public void onUninstall(Contribution contribution) {
        // no-op
    }

    public void onRemove(Contribution contribution) {
        // no-op
    }
}
