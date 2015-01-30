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

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.JavaImport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.oasisopen.sca.annotation.Property;

/**
 * Adds an implicit import of the Hibernate contribution extension into any contribution using JPA on a runtime configured to use Hibernate. This is
 * necessary as Hibernate's use of CGLIB for generating proxies requires that Hibernate classes (specifically, HibernateDelegate) be visible from the
 * classloader that loaded a particular entity (i.e. the application classloader). If a Hibernate is explicitly imported in a contribution manifest
 * (sca-contribution.xml), it is used instead.
 */
public class HibernateContributionListener implements ContributionServiceListener {
    private JavaImport hibernateImport;
    private JavaImport javassistImport;
    private boolean noImplicitImport;
    private JavaImport apiImport;

    public HibernateContributionListener() {
        PackageInfo hibernateInfo = new PackageInfo("org.hibernate.*");
        hibernateImport = new JavaImport(hibernateInfo);
        PackageInfo javassistInfo = new PackageInfo("javassist.util.proxy");
        javassistImport = new JavaImport(javassistInfo);
        PackageInfo apiInfo = new PackageInfo("org.fabric3.jpa.api");
        apiImport = new JavaImport(apiInfo);
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
        boolean jpaImported = false;
        boolean hibernateImported = false;
        boolean javassistImported = false;
        ContributionManifest manifest = contribution.getManifest();
        for (Import imprt : manifest.getImports()) {
            if (imprt instanceof JavaImport) {
                JavaImport contributionImport = (JavaImport) imprt;
                String name = contributionImport.getPackageInfo().getName();
                if (name.equals("org.hibernate.*")) {
                    // already explicitly imported
                    hibernateImported = true;
                } else if (name.startsWith("javassist.")) {
                    // already explicitly imported
                    javassistImported = true;
                } else if (contributionImport.getPackageInfo().getName().startsWith("javax.persistence")) {
                    jpaImported = true;
                }
            }
        }
        if (jpaImported) {
            // JPA is imported, add implicit Hibernate and Javasssist imports
            if (!hibernateImported) {
                manifest.addImport(hibernateImport);
            }
            if (!javassistImported) {
                manifest.addImport(javassistImport);
            }
            manifest.addImport(apiImport);
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
