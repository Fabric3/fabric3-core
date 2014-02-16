/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
        // This way EhCache is imported always???
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
